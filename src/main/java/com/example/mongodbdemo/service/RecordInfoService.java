package com.example.mongodbdemo.service;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mongodbdemo.cache.RecordInfoResourceCache;
import com.example.mongodbdemo.content.CommonContent;
import com.example.mongodbdemo.data.bo.*;
import com.example.mongodbdemo.data.bo.excel.ConsumerResourceExcelBO;
import com.example.mongodbdemo.data.bo.excel.RecordOrderInfoExcelBO;
import com.example.mongodbdemo.data.dto.*;
import com.example.mongodbdemo.data.to.RecordInfoResourceCacheTO;
import com.example.mongodbdemo.data.vo.*;
import com.example.mongodbdemo.entity.*;
import com.example.mongodbdemo.enums.ConsumerResource;
import com.example.mongodbdemo.enums.EncodeMode;
import com.example.mongodbdemo.enums.StorageType;
import com.example.mongodbdemo.enums.StorageVersionType;
import com.example.mongodbdemo.excepition.ResultException;
import com.example.mongodbdemo.oss.service.OSSService;
import com.example.mongodbdemo.service.mq.MQSenderService;
import com.example.mongodbdemo.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.example.mongodbdemo.content.CommonContent.*;
import static com.example.mongodbdemo.enums.EncodeMode.ENCODE_AES;

/**
 * description:
 * Created by whq on 2020/7/23
 */
@Slf4j
@Service
public class RecordInfoService extends BaseMultithreadService {
//    @Resource
//    private RecordInfoDao recordInfoDao;
    @Autowired
    private OperationLogService operationLogService;
//    @Autowired
//    private OperationLogDao operationLogDao;
//    @Autowired
//    private RecordSourceDao recordSourceDao;
    @Autowired
    private OperationVersionBasicInfoService operationVersionBasicInfoService;
//    @Autowired
//    private RecordInfoExtDao recordInfoExtDao;
//    @Autowired
//    private FailedOrderInfoDao failedOrderInfoDao;
//    @Autowired
//    private AgencyDao agencyDao;
    @Autowired
    private RecordInfoResolverHandler recordInfoResolverHandler;
//    @Autowired
//    private OperationVersionAppletProductDao appletProductDao;
//    @Autowired
//    private OperationVersionAppletImageDao appletImageDao;
    @Autowired
    private JSAESUtils jsaesUtils;
    @Autowired
    private RecordInfoDecryptErrorService recordInfoDecryptErrorService;
    @Autowired
    private StorageType storageType;
    @Autowired
    private RecordInfoMultiStorageService recordInfoMultiStorageService;
//    @Autowired
//    private TaskInfoDao taskInfoDao;
//    @Autowired
//    private OrderInfoDao orderInfoDao;
    @Autowired
    private RecordInfoResourceCache recordInfoResourceCache;
    @Autowired
    private OSSService myOssService;
//    @Autowired
//    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private RedisLockService lock;
    @Autowired
    private ReturnInfoService returnInfoService;
    @Autowired
    private StorageVersionType storageVersionType;
//    @Autowired
//    private OperationVersionInfoMapper operationVersionInfoMapper;
    @Autowired
    private MQSenderService mqSenderService;

//    @Autowired
//    private OrderMissTaskDao orderMissTaskDao;

    private static ThreadLocal<SimpleDateFormat> beginDateFomratThreadLocal
            = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd 00:00:00.000"));

    private static ThreadLocal<SimpleDateFormat> endDateFomratThreadLocal
            = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd 23:59:59.999"));

    private static ThreadLocal<SimpleDateFormat> simpleDateFomratThreadLocal
            = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

    public RecordInfoVO getRecordInfo(GetRecordInfoDTO getRecordInfoDTO) {
        String taskId = getRecordInfoDTO.getTaskId();
        List<String> recordInfo = recordInfoMultiStorageService.getRecordInfoList(taskId);
        RecordInfoVO recordInfoVO = new RecordInfoVO();
        recordInfoVO.setRecordInfo(recordInfo);
        return recordInfoVO;
    }

    public RecordInfoVO getRecordInfoPage(GetRecordInfoDTO getRecordInfoDTO) {
        String taskId = getRecordInfoDTO.getTaskId();
        Integer encodeMode = null;
//        Integer encodeMode = taskInfoDao.getEncodeMode(taskId);
        if (encodeMode == null) {
            encodeMode = ENCODE_AES.getValue();
        }
        int pos = getRecordInfoDTO.getPos() - 1;
        int offset = pos * getRecordInfoDTO.getSize();
//        List<String> recordInfo = recordInfoDao.queryRecordInfoPageByTaskId(taskId, offset, getRecordInfoDTO.getSize());
        List<String> recordInfo = recordInfoMultiStorageService.queryRecordInfoPageByTaskId(taskId, offset, getRecordInfoDTO.getSize());
        RecordInfoVO recordInfoVO = new RecordInfoVO();
        recordInfoVO.setEncodeMode(encodeMode);
        recordInfoVO.setRecordInfo(recordInfo);
        if (recordInfo == null) {
            recordInfoVO.setFinished(true);
            return recordInfoVO;
        }
        if (recordInfo.size() < getRecordInfoDTO.getSize()) {
            recordInfoVO.setFinished(true);
        } else {
            recordInfoVO.setFinished(false);
        }

        return recordInfoVO;
    }


    public TaskDetailVO getTaskDetail(GetTaskDetailDTO getTaskDetailDTO) {
        String taskId = getTaskDetailDTO.getTaskId();
        TaskInfoEntity taskInfoEntity = null;
//        TaskInfoEntity taskInfoEntity = taskInfoDao.findTaskInfoByTaskId(taskId);
        OrderInfoDTO orderInfoDTO = new OrderInfoDTO();
        String extraInfo = StringUtils.EMPTY;
        if (Objects.nonNull(taskInfoEntity)) {
            //????????????
            BeanUtils.copyProperties(taskInfoEntity, orderInfoDTO);
            orderInfoDTO.setPolicyHolder(taskInfoEntity.getApplicantName());
            orderInfoDTO.setInsuranceNo(taskInfoEntity.getPolicyId());
            //????????????
            extraInfo = taskInfoEntity.getExtraInfo();
        }

        //????????????
        List<OperationLogEntity> operationLogEns = operationLogService.getAllByTaskId(taskId);
        List<OperationInfoVO> operationInfoVOS = operationLogEns.stream().map(operationLog -> {
            OperationInfoVO operationInfoDTO = new OperationInfoVO();
            operationInfoDTO.setCode(operationLog.getActCode());
            operationInfoDTO.setCodeDesc(operationLog.getActDesc());
            operationInfoDTO.setMsg(operationLog.getMsg());
            operationInfoDTO.setCreateTime(operationLog.getCreateTime());
            return operationInfoDTO;
        }).collect(Collectors.toList());

        //????????????
        RecordResourceVO resourceInfo = resourceInfoProcess(taskId);

        TaskDetailVO taskDetailVO = new TaskDetailVO();
        taskDetailVO.setOrderInfo(orderInfoDTO);
        taskDetailVO.setExtraInfo(extraInfo);
        taskDetailVO.setOperationInfo(operationInfoVOS);
        taskDetailVO.setResourceInfo(resourceInfo);
        return taskDetailVO;
    }

    /**
     * ??????????????????
     *
     * @param taskId
     * @return
     */
    public RecordResourceVO resourceInfoProcess(String taskId) {
        List<Integer> ids = null;
//        List<Integer> ids = recordSourceDao.findIdsByTaskId(taskId);
        List<RecordResourceEntity> resourceEntities = null;
//        List<RecordResourceEntity> resourceEntities = recordSourceDao.findAllByIds(ids);
        RecordResourceVO recordResourceVO = new RecordResourceVO();
        //????????????????????????
        Map<String, List<RecordResourceEntity>> collectByGroupSourceType = resourceEntities.stream().collect(Collectors.groupingBy(RecordResourceEntity::getResourceType));
        // ??????image
        List<RecordResourceEntity> imageResourceEntities = Optional.ofNullable(collectByGroupSourceType.get(CommonContent.IMAGE)).orElse(new ArrayList<>());
        // ??????title??????
        Map<String, List<RecordResourceEntity>> collectByGroupTitle = imageResourceEntities.stream().collect(Collectors.groupingBy(RecordResourceEntity::getTitle));
        Set<Map.Entry<String, List<RecordResourceEntity>>> entries = collectByGroupTitle.entrySet();
        for (Map.Entry<String, List<RecordResourceEntity>> entry : entries) {
            ImageResourceVO imageResourceVO = new ImageResourceVO();
            imageResourceVO.setTitle(entry.getKey());
            List<RecordResourceEntity> entryValue = entry.getValue();
            // ??????video resource??????
            List<ImageResourceInfoVO> collect = entryValue.stream().map(recordResourceEntity -> {
                ImageResourceInfoVO imageResourceInfoVO = new ImageResourceInfoVO();
                imageResourceInfoVO.setName(recordResourceEntity.getResourceName());
                imageResourceInfoVO.setEncode(recordResourceEntity.getResource());
                return imageResourceInfoVO;
            }).collect(Collectors.toList());
            // ??????title????????????image??????
            imageResourceVO.setImageList(collect);
            // ????????????title
            recordResourceVO.getImage().add(imageResourceVO);
        }

        // ??????video
        List<RecordResourceEntity> videoResourceEntities = Optional.ofNullable(collectByGroupSourceType.get(CommonContent.VIDEO)).orElse(new ArrayList<>());
        // title????????????????????????title
        Map<String, List<RecordResourceEntity>> videoCollectByGroupTitle = videoResourceEntities.stream().collect(Collectors.groupingBy(RecordResourceEntity::getTitle));
        for (Map.Entry<String, List<RecordResourceEntity>> entry : videoCollectByGroupTitle.entrySet()) {
            VideoResourceVO videoResourceVO = new VideoResourceVO();
            videoResourceVO.setTitle(entry.getKey());
            // ???????????????resource????????????
            List<VideoResourceInfoVO> collect = entry.getValue().stream().map(recordResourceEntity -> {
                VideoResourceInfoVO videoResourceInfoVO = new VideoResourceInfoVO();
                videoResourceInfoVO.setName(recordResourceEntity.getResourceName());
                videoResourceInfoVO.setPath(recordResourceEntity.getResource());
                return videoResourceInfoVO;
            }).collect(Collectors.toList());
            // ??????title????????????video??????
            videoResourceVO.setVideoList(collect);
            // ????????????title
            recordResourceVO.getVideo().add(videoResourceVO);
        }
        return recordResourceVO;


    }

    /**
     * MQ ??????????????????
     *
     * @param recordInfoDTO
     * @return
     */
    public Result<CreateRecordInfoVO> recordOptionAsyncMQHandler(RecordInfoDTO recordInfoDTO) {
        if (recordInfoDTO.getLast().equals(1) && null == recordInfoDTO.getOrderInfo()) {
            // ?????????????????????????????????
            saveFailedOrder(recordInfoDTO, RespCode.ERROR_4_.getMsg());
            throw new ResultException(RespCode.ERROR_4_, "???last???1?????????????????????????????????");
        }
        // ????????????
        String orderId = recordInfoDTO.getOrderInfo() != null ? recordInfoDTO.getOrderInfo().getOrderId() : "???????????????orderId";
        log.info("service-recording???????????????????????????, taskId={}, orderId = {}, index ={}, last={} ", recordInfoDTO.getTaskId(), orderId, recordInfoDTO.getIndex(), recordInfoDTO.getLast());

        if (emptyEvent(recordInfoDTO.getRecordInfo()) && (recordInfoDTO.getOrderInfo() == null)) {
            log.warn("?????????????????????,?????????????????????????????????????????????????????????");
            return Result.success(new CreateRecordInfoVO(recordInfoDTO.getTaskId()));
        }
        // ??????MQ
        long timeMillis = System.currentTimeMillis();
        mqSenderService.sendRecordInfo(recordInfoDTO);
        log.info("recording???????????????MQ??????,taskId={}, orderId = {}, index ={}, last={} ??????={} ??????", recordInfoDTO.getTaskId(), orderId, recordInfoDTO.getIndex(), recordInfoDTO.getLast(), System.currentTimeMillis() - timeMillis);
        return Result.success(new CreateRecordInfoVO(recordInfoDTO.getTaskId()));
    }

    /**
     * ?????????????????????????????????
     *
     * @param recordInfoDTO
     */
    public void recordOptionReadyCAS(RecordInfoDTO recordInfoDTO) {
        log.info("recording ??????????????????????????????...");
        try {
            String requestId = String.format("%s_%d", StUtils.getUUID(), Thread.currentThread().getId());
            String orderId = recordInfoDTO.getOrderInfo() != null ? recordInfoDTO.getOrderInfo().getOrderId() : "???????????????orderId";
            log.info("MQ???????????????task???????????????taskId:{}???requestId:{}???orderId = {}, index ={}, last={} ", recordInfoDTO.getTaskId(), Thread.currentThread().getId(), orderId, recordInfoDTO.getIndex(), recordInfoDTO.getLast());
            boolean locked = false;
            while (!locked) {
                log.info("????????????taskId??????taskid:{},RequestId:{}???orderId = {}, index ={}, last={} ", recordInfoDTO.getTaskId(), requestId, orderId, recordInfoDTO.getIndex(), recordInfoDTO.getLast());
                locked = lock.lockTask(recordInfoDTO.getTaskId(), requestId);
            }
            if (locked) {
                try {
                    log.info("??????taskId????????????taskid:{},RequestId:{}???orderId = {}, index ={}, last={} ", recordInfoDTO.getTaskId(), requestId, orderId, recordInfoDTO.getIndex(), recordInfoDTO.getLast());
                    recordOption(recordInfoDTO);
                } finally {
                    lock.releaseTask(recordInfoDTO.getTaskId(), requestId);
                }
            }
            log.info("MQ???????????????taskId?????????????????????taskId:{},RequestId:{}???orderId = {}, index ={}, last={} ", recordInfoDTO.getTaskId(), requestId, orderId, recordInfoDTO.getIndex(), recordInfoDTO.getLast());
        } catch (Exception e) {
            saveFailedOrder(recordInfoDTO, e.getMessage());
            log.error("??????recording ?????????????????????recordInfoId:{},??????:{},", recordInfoDTO.getTaskId(), JsonUtils.toJson(recordInfoDTO), e);
            e.printStackTrace();
        }

    }

    public Result<CreateRecordInfoVO> recordOptionHandler(RecordInfoDTO recordInfoDTO) {
        if (recordInfoDTO.getLast().equals(1) && null == recordInfoDTO.getOrderInfo()) {
            // ?????????????????????????????????
            saveFailedOrder(recordInfoDTO, RespCode.ERROR_4_.getMsg());
            throw new ResultException(RespCode.ERROR_4_, "???last???1?????????????????????????????????");
        }

        if (emptyEvent(recordInfoDTO.getRecordInfo()) && (recordInfoDTO.getOrderInfo() == null)) {
            log.warn("?????????????????????,?????????????????????????????????????????????????????????");
            return Result.success(new CreateRecordInfoVO(recordInfoDTO.getTaskId()));
        }
        log.info("???????????????????????????taskid:{}", recordInfoDTO.getTaskId());
        threadPool.execute(() -> {
            log.info("recording ??????????????????????????????...");
            try {
                String requestId = String.format("%s_%d", StUtils.getUUID(), Thread.currentThread().getId());
                log.info("???????????????task?????????taskid:{}???requestId:{}", recordInfoDTO.getTaskId(), Thread.currentThread().getId());
                boolean locked = false;
                while (!locked) {
                    locked = lock.lockTask(recordInfoDTO.getTaskId(), requestId);
                }
                if (locked) {
                    try {
                        log.info("??????taskId????????????taskid:{},RequestId:{}", recordInfoDTO.getTaskId(), requestId);
                        recordOption(recordInfoDTO);
                    } finally {
                        lock.releaseTask(recordInfoDTO.getTaskId(), requestId);
                    }
                }
                log.info("??????????????????????????????????????????...");
            } catch (Exception e) {
                saveFailedOrder(recordInfoDTO, e.getMessage());
                log.error("??????recording ?????????????????????recordInfoId:{},??????:{},", recordInfoDTO.getTaskId(), JsonUtils.toJson(recordInfoDTO), e);
                e.printStackTrace();
            }
        });
        log.info("??????????????????????????????????????????...");
        CreateRecordInfoVO createRecordInfoVO = new CreateRecordInfoVO(recordInfoDTO.getTaskId());
        return Result.success(createRecordInfoVO);
    }

    private void saveFailedOrder(RecordInfoDTO recordInfoDTO, String Msg) {
        FailedOrderInfoEntity failedOrderInfoEntity = new FailedOrderInfoEntity();
        failedOrderInfoEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        failedOrderInfoEntity.setMsg(Msg);
        failedOrderInfoEntity.setTaskId(recordInfoDTO.getTaskId());
        if (null != recordInfoDTO.getOrderInfo() && null != recordInfoDTO.getOrderInfo().getOrderId()) {
            failedOrderInfoEntity.setOrderId(recordInfoDTO.getOrderInfo().getOrderId());
        }
//        failedOrderInfoDao.save(failedOrderInfoEntity);
    }

    private void saveIndexFailedOrder(RecordInfoDTO recordInfoDTO) {

        FailedOrderInfoEntity failedOrderInfoEntity = new FailedOrderInfoEntity();
        failedOrderInfoEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        failedOrderInfoEntity.setMsg("??????????????????");
        failedOrderInfoEntity.setTaskId(recordInfoDTO.getTaskId());
        if (null != recordInfoDTO.getOrderInfo() && null != recordInfoDTO.getOrderInfo().getOrderId()) {
            failedOrderInfoEntity.setOrderId(recordInfoDTO.getOrderInfo().getOrderId());
            if (null != recordInfoDTO.getOrderInfo().getInsuranceNo()) {
                failedOrderInfoEntity.setInsuranceNo(recordInfoDTO.getOrderInfo().getInsuranceNo());
            }
        }
//        failedOrderInfoDao.save(failedOrderInfoEntity);
    }


    public Result<CreateRecordInfoVO> createRecordOptionHandler(CreateOrderInfoDTO createOrderInfoDTO) {
        RecordInfoDTO recordInfoDTO = new RecordInfoDTO();
        if (null == createOrderInfoDTO.getTaskId()) {
            throw new ResultException(RespCode.ERROR_12_, "?????????taskId");
        }
        recordInfoDTO.setTaskId(createOrderInfoDTO.getTaskId());
        recordInfoDTO.setOrderInfo(createOrderInfoDTO.getOrderInfo());
        recordInfoDTO.setLast(1);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        recordInfoDTO.setTimestamp(timestamp.toString());
        try {
            String requestId = String.format("%s_%d", StUtils.getUUID(), Thread.currentThread().getId());
            boolean locked = false;
            while (!locked) {
                locked = lock.lockTask(recordInfoDTO.getTaskId(), requestId);
            }
            if (locked) {
                try {
                    recordOption(recordInfoDTO);
                } finally {
                    lock.releaseTask(recordInfoDTO.getTaskId(), requestId);
                }
            }
        } catch (Exception e) {
            saveFailedOrder(recordInfoDTO, RespCode.ERROR_4_.getMsg());
            log.error("??????createRecordOptionHandler???????????????:{}", JsonUtils.toJson(createOrderInfoDTO), e);
        }
        log.info("??????????????????????????????????????????...");
        CreateRecordInfoVO createRecordInfoVO = new CreateRecordInfoVO(recordInfoDTO.getTaskId());
        return Result.success(createRecordInfoVO);
    }

    /**
     * ???????????????
     *
     * @param recordInfoDTO
     * @return
     * @author Logan
     * @date 2020-11-10 14:49
     */
    private void setDefaultValue(RecordInfoDTO recordInfoDTO) {
        if (recordInfoDTO.getEncodeMode() == null) {
            recordInfoDTO.setEncodeMode(ENCODE_AES.getValue());
        }
    }

    public void recordOption(RecordInfoDTO recordInfoDTO) {
        setDefaultValue(recordInfoDTO);
        OrderInfoDTO orderInfoDTO = recordInfoDTO.getOrderInfo();
        String myOrderId = null;
        if (orderInfoDTO != null) {
            myOrderId = recordInfoDTO.getOrderInfo().getOrderId();
            log.info("??????taskId??????, orderId:{}, taskId:{}", myOrderId, recordInfoDTO.getTaskId());
        }
        TaskInfoEntity taskInfoEntity = null;
//        TaskInfoEntity taskInfoEntity = taskInfoDao.findTaskInfoByTaskId(recordInfoDTO.getTaskId());
        if (taskInfoEntity == null) {
            taskInfoEntity = new TaskInfoEntity();

        }
        // ??????taskInfo??????
        String platform = "";
        String productCode = "";
        String channel = "";
        String orderId = null;
        if (orderInfoDTO != null) {
            platform = orderInfoDTO.getPlatform();
            productCode = orderInfoDTO.getProductCode();
            channel = orderInfoDTO.getChannel();
            orderId = orderInfoDTO.getOrderId();
        }
        if (StringUtils.isEmpty(platform)) {
            platform = taskInfoEntity.getPlatform();
        }
        if (StringUtils.isEmpty(productCode)) {
            productCode = taskInfoEntity.getProductCode();
        }
        if (StringUtils.isEmpty(channel)) {
            channel = taskInfoEntity.getChannel();
        }
        if (StringUtils.isNotBlank(taskInfoEntity.getOrderId())) {
            if (changedOrderIdInSameTask(orderId, taskInfoEntity.getOrderId())) {
                //??????order_info???ordr_id?????????????????????????????????order_miss_task
//                clearOldOrderIdAndMark(taskInfoEntity.getOrderId(), taskInfoEntity.getId());
                clearOldOrderIdAndMark(taskInfoEntity.getOrderId(), null);
            }
        }
        Integer versionId = getVersionId(taskInfoEntity.getProductCode(), taskInfoEntity.getPlatform(), taskInfoEntity.getChannel());
        //taskInfo
        log.info("taskId?????????orderId:{},beforeSaveTask: taskInfoEntity.getOrderId():{},taskId:{}", orderId, taskInfoEntity.getOrderId(), recordInfoDTO.getTaskId());
        saveTask(taskInfoEntity, recordInfoDTO, versionId);
        log.info("taskId?????????orderId:{},afterSaveTask: taskInfoEntity.getOrderId():{},taskId:{}", orderId, taskInfoEntity.getOrderId(), recordInfoDTO.getTaskId());
        //????????????????????????
//        saveRecordInfo(recordInfoDTO, orderId, platform, taskInfoEntity.getId(), productCode, channel);
        saveRecordInfo(recordInfoDTO, orderId, platform, null, productCode, channel);

        if (1 == recordInfoDTO.getLast()) {
            log.info("??????id???{}??? taskId???{}?????????????????????????????????...", orderId, recordInfoDTO.getTaskId());
        } else {
            log.info("taskId???{}???????????????????????????...", recordInfoDTO.getTaskId());
        }
        //orderInfo
        log.info("taskId?????????orderId:{},beforeCreateOrder: taskInfoEntity.getOrderId():{},taskId:{}", orderId, taskInfoEntity.getOrderId(), recordInfoDTO.getTaskId());
        createOrder(recordInfoDTO.getOrderInfo(), taskInfoEntity, getCreateTime(recordInfoDTO.getTimestamp()), versionId);
        log.info("taskId?????????orderId:{},afterCreateOrder: taskInfoEntity.getOrderId():{},taskId:{}", orderId, taskInfoEntity.getOrderId(), recordInfoDTO.getTaskId());
        if (1 == recordInfoDTO.getLast()) {
            fillInOperation(recordInfoDTO.getOrderInfo(), taskInfoEntity.getTaskId());
        }
    }

    private void fillInOperation(OrderInfoDTO orderInfo, String taskId) {
        String productName = orderInfo.getProductName();
        String productCode = orderInfo.getProductCode();
        List<OperationLogEntity> allByTaskId = null;
//        List<OperationLogEntity> allByTaskId = operationLogDao.findAllByTaskId(taskId);
        List<OperationLogEntity> collect = allByTaskId.stream().map(operationLogEntity -> {
            operationLogEntity.setProductName(productName);
            operationLogEntity.setProductCode(productCode);
            return operationLogEntity;
        }).collect(Collectors.toList());
//        operationLogDao.saveAll(collect);
    }

    /**
     * ?????????TaskId??????????????????orderId
     *
     * @param newOrderId
     * @param taskOrderId
     * @return
     * @author Logan
     * @date 2020-11-18 20:55
     */
    private boolean changedOrderIdInSameTask(String newOrderId, String taskOrderId) {
        return StringUtils.isNotBlank(taskOrderId) && StringUtils.isNotBlank(newOrderId) && !taskOrderId.equals(newOrderId);
    }


    /**
     * ?????????order_id??????????????????????????????????????????????????????????????????
     *
     * @param oldOrderId
     * @param taskInfoId
     * @return
     * @author Logan
     * @date 2020-11-18 20:55
     */
    private void clearOldOrderIdAndMark(String oldOrderId, Integer taskInfoId) {
        OrderInfoEntity orderInfoEntity = null;
//        OrderInfoEntity orderInfoEntity = orderInfoDao.findOrderInfoEntityByOrderId(oldOrderId);
        if (orderInfoEntity == null || orderInfoEntity.getId() == null) {
            return;
        }
        Integer orderInfoId = orderInfoEntity.getId();
        orderInfoEntity.setOrderId("");
//        orderInfoDao.save(orderInfoEntity);
        log.info("recording????????????????????????????????????orderInfo??????????????????order_Id??????????????????");
        markTaskMissedOrderId(orderInfoId, oldOrderId, taskInfoId);
    }

    private void markTaskMissedOrderId(Integer orderInfoId, String orderId, Integer taskInfoId) {
        try {
            OrderMissTaskEntity orderMissTaskEntity = new OrderMissTaskEntity();
            orderMissTaskEntity.setOrderId(orderId);
            orderMissTaskEntity.setOrderInfoId(orderInfoId);
            orderMissTaskEntity.setTaskInfoId(taskInfoId);
//            orderMissTaskDao.insert(orderMissTaskEntity);
        } catch (Exception e) {
            log.warn("??????OrderMissTask?????????", e);
        }
    }

    /**
     * ??????Task??????
     *
     * @param recordInfoDTO
     * @return
     * @author Logan
     * @date 2020-10-15 14:22
     */
    private void saveTask(TaskInfoEntity taskInfoEntity, RecordInfoDTO recordInfoDTO, Integer versionId) {
        OrderInfoDTO orderInfoDTO = recordInfoDTO.getOrderInfo();
        taskInfoEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        taskInfoEntity.setTaskId(recordInfoDTO.getTaskId());
        taskInfoEntity.setLast(recordInfoDTO.getLast());
        taskInfoEntity.setFileDownloadUri(recordInfoDTO.getFileDownloadUri());
        taskInfoEntity.setEncodeMode(recordInfoDTO.getEncodeMode());

        // ????????? ??????-??????-????????????????????????,??????????????????
        if (storageVersionType.name().equals(StorageVersionType.PRODUCT_GRANULARITY_VERSION_TYPE.name())) {
            taskInfoEntity.setProductGranularityVersionId(versionId);

        } else {
            // ????????? ????????????????????????????????????
            taskInfoEntity.setVersionId(versionId);
        }

        // ??????????????????????????????
        if (recordInfoDTO.getOptraceFile() != null && recordInfoDTO.getOptraceFile().equals(1)) {
            taskInfoEntity.setOptraceFile(1);
        }
        copyTaskField(taskInfoEntity, orderInfoDTO, getCreateTime(recordInfoDTO.getTimestamp()));
        taskInfoEntity.setUploadTime(getCreateTime(recordInfoDTO.getTimestamp()));
//        taskInfoDao.save(taskInfoEntity);
    }

    /**
     * ??????????????????
     *
     * @param recordInfoDTO
     * @param orderId
     * @param platform
     * @param tiId
     * @return
     * @author Logan
     * @date 2020-10-15 14:21
     */
    private void saveRecordInfo(RecordInfoDTO recordInfoDTO, String orderId, String platform, Integer tiId, String productCode, String channel) {
        if (!emptyEvent(recordInfoDTO.getRecordInfo()) && Objects.nonNull(recordInfoDTO.getRecordInfo())) {
            RecordInfoMultiStorageBO recordInfoMultiStorageBO = new RecordInfoMultiStorageBO();
            //recordInfo
            recordInfoMultiStorageBO.setLast(recordInfoDTO.getLast());
            recordInfoMultiStorageBO.setTaskId(recordInfoDTO.getTaskId());
            recordInfoMultiStorageBO.setRecordInfo(recordInfoDTO.getRecordInfo());
            recordInfoMultiStorageBO.setTiId(tiId);
            recordInfoMultiStorageBO.setOrderId(orderId);
            recordInfoMultiStorageBO.setUploadTime(getCreateTime(recordInfoDTO.getTimestamp()));
            recordInfoMultiStorageBO.setIndex(recordInfoDTO.getIndex());
            recordInfoMultiStorageBO.setRecordMode(recordInfoDTO.getMode());
            Integer recordInfoId = recordInfoMultiStorageService.saveRecordInfo(recordInfoMultiStorageBO);

            log.info("?????????????????????????????????taskid:{},recordId:{}", recordInfoDTO.getTaskId(), recordInfoId);
            // ??????index?????????????????????
            if (recordInfoDTO.getLast().equals(1)) {
                Integer indexCount = recordInfoMultiStorageService.IndexCheck(recordInfoMultiStorageBO);
                if (indexCount >= 0 && !indexCount.equals(recordInfoDTO.getIndex())) {
                    log.info("?????????????????????taskid:{},recordId:{}", recordInfoDTO.getTaskId(), recordInfoId);
                    saveIndexFailedOrder(recordInfoDTO);
                }
            }
            // ??????recordInfo ??????????????????,???????????????????????????????????????
            saveRecordInfoResourceCache(recordInfoId, productCode, channel, platform);
        }
    }

    /**
     * ??????Order
     *
     * @param orderInfoDTO
     * @param updateTimeStamp ???????????????
     * @param versionId       ?????????
     * @return
     * @author Logan
     * @date 2020-10-15 11:49
     */
    private void createOrder(OrderInfoDTO orderInfoDTO, TaskInfoEntity taskInfoEntity, Timestamp updateTimeStamp, Integer versionId) {
        if (orderInfoDTO == null) {
            return;
        }
        String requestId = String.format("%s_%d", StUtils.getUUID(), Thread.currentThread().getId());
        boolean locked = false;
        while (!locked) {
            locked = lock.lockOrder(orderInfoDTO.getOrderId(), requestId);
        }
        if (locked) {
            try {
                mergeTaskInfoToOrderInfo(orderInfoDTO, taskInfoEntity, updateTimeStamp);
                OrderInfoEntity orderInfoEntity = null;
                if (orderInfoDTO.getOrderId() == null || orderInfoDTO.getOrderId() == "") {
                    orderInfoEntity = new OrderInfoEntity();
                } else {
                    orderInfoEntity = null;
//                    orderInfoEntity = orderInfoDao.findOrderInfoByOrderId(orderInfoDTO.getOrderId());
                    if (orderInfoEntity == null) {
                        orderInfoEntity = new OrderInfoEntity();
                        orderInfoEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
                        orderInfoEntity.setUploadTime(updateTimeStamp);
                    }
                }
                copyOrderInfoDTO(orderInfoDTO, orderInfoEntity, updateTimeStamp, versionId);
//                orderInfoDao.save(orderInfoEntity);
            } finally {
                lock.releaseOrder(orderInfoDTO.getOrderId(), requestId);
            }
        }
    }

    /**
     * ???????????????task_info???????????????OrderDTO??????
     *
     * @param orderInfoDTO
     * @param taskInfoEntity
     * @param updateTimeStamp
     * @return
     * @author Logan
     * @date 2020-11-06 16:21
     */
    private void mergeTaskInfoToOrderInfo(OrderInfoDTO orderInfoDTO, TaskInfoEntity taskInfoEntity, Timestamp updateTimeStamp) {
        mergeOrderDTONullField(orderInfoDTO, taskInfoEntity);
        updateOrderDTOField(orderInfoDTO, taskInfoEntity, updateTimeStamp);
    }

    private void updateOrderDTOField(OrderInfoDTO orderInfoDTO, TaskInfoEntity taskInfoEntity, Timestamp updateTimeStamp) {
        if (updateTimeStamp.after(taskInfoEntity.getUploadTime()) || updateTimeStamp.equals(taskInfoEntity.getUploadTime())) {
            if (StringUtils.isNotEmpty(taskInfoEntity.getOrderId())) {
                orderInfoDTO.setOrderId(taskInfoEntity.getOrderId());
            }
            if (StringUtils.isNotEmpty(taskInfoEntity.getAgencyCode())) {
                orderInfoDTO.setAgencyCode(taskInfoEntity.getAgencyCode());
            }
            if (StringUtils.isNotEmpty(taskInfoEntity.getAccount())) {
                orderInfoDTO.setAccount(taskInfoEntity.getAccount());
            }
            if (StringUtils.isNotEmpty(taskInfoEntity.getAgencyName())) {
                orderInfoDTO.setAgencyName(taskInfoEntity.getAgencyName());
            }
            if (StringUtils.isNotEmpty(taskInfoEntity.getBusinessType())) {
                orderInfoDTO.setBusinessType(taskInfoEntity.getBusinessType());
            }
            if (StringUtils.isNotEmpty(taskInfoEntity.getChannel())) {
                orderInfoDTO.setChannel(taskInfoEntity.getChannel());
            }
            if (StringUtils.isNotEmpty(taskInfoEntity.getExtraInfo())) {
                orderInfoDTO.setExtraInfo(taskInfoEntity.getExtraInfo());
            }
            if (StringUtils.isNotEmpty(taskInfoEntity.getPolicyId())) {
                orderInfoDTO.setInsuranceNo(taskInfoEntity.getPolicyId());
            }
            if (StringUtils.isNotEmpty(taskInfoEntity.getApplicantName())) {
                orderInfoDTO.setPolicyHolder(taskInfoEntity.getApplicantName());
            }
            if (StringUtils.isNotEmpty(taskInfoEntity.getPlatform())) {
                orderInfoDTO.setPlatform(taskInfoEntity.getPlatform());
            }
            if (StringUtils.isNotEmpty(taskInfoEntity.getProductCode())) {
                orderInfoDTO.setProductCode(taskInfoEntity.getProductCode());
            }
            if (StringUtils.isNotEmpty(taskInfoEntity.getProductName())) {
                orderInfoDTO.setProductName(taskInfoEntity.getProductName());
            }
        }
    }

    private void mergeOrderDTONullField(OrderInfoDTO orderInfoDTO, TaskInfoEntity taskInfoEntity) {
        if (StringUtils.isEmpty(orderInfoDTO.getAgencyCode())) {
            orderInfoDTO.setAgencyCode(taskInfoEntity.getAgencyCode());
        }
        if (StringUtils.isEmpty(orderInfoDTO.getAccount())) {
            orderInfoDTO.setAccount(taskInfoEntity.getAccount());
        }
        if (StringUtils.isEmpty(orderInfoDTO.getAgencyName())) {
            orderInfoDTO.setAgencyName(taskInfoEntity.getAgencyName());
        }
        if (StringUtils.isEmpty(orderInfoDTO.getBusinessType())) {
            orderInfoDTO.setBusinessType(taskInfoEntity.getBusinessType());
        }
        if (StringUtils.isEmpty(orderInfoDTO.getChannel())) {
            orderInfoDTO.setChannel(taskInfoEntity.getChannel());
        }
        if (StringUtils.isEmpty(orderInfoDTO.getExtraInfo())) {
            orderInfoDTO.setExtraInfo(taskInfoEntity.getExtraInfo());
        }
        if (StringUtils.isEmpty(orderInfoDTO.getInsuranceNo())) {
            orderInfoDTO.setInsuranceNo(taskInfoEntity.getPolicyId());
        }
        if (StringUtils.isEmpty(orderInfoDTO.getPolicyHolder())) {
            orderInfoDTO.setPolicyHolder(taskInfoEntity.getApplicantName());
        }
        if (StringUtils.isEmpty(orderInfoDTO.getPlatform())) {
            orderInfoDTO.setPlatform(taskInfoEntity.getPlatform());
        }
        if (StringUtils.isEmpty(orderInfoDTO.getProductCode())) {
            orderInfoDTO.setProductCode(taskInfoEntity.getProductCode());
        }
        if (StringUtils.isEmpty(orderInfoDTO.getProductName())) {
            orderInfoDTO.setProductName(taskInfoEntity.getProductName());
        }
    }

    private void copyTaskField(TaskInfoEntity taskInfoEntity, OrderInfoDTO orderInfoDTO, Timestamp updateTime) {
        if (orderInfoDTO == null) {
            return;
        }
        copyTaskFieldNullInDB(taskInfoEntity, orderInfoDTO);
        if (taskInfoEntity.getUploadTime() != null) {
            if (taskInfoEntity.getUploadTime().before(updateTime)) {
                //???????????????????????????????????????????????????????????????
                copyTaskFieldReplaceDB(taskInfoEntity, orderInfoDTO);
            }
        }
    }

    private void copyTaskFieldReplaceDB(TaskInfoEntity taskInfoEntity, OrderInfoDTO orderInfoDTO) {
        log.info("??????taskId??????, orderInfoDTO.getOrderId:{},taskId:{},taskInfoEntity.getOrderId():{}", orderInfoDTO.getOrderId(), taskInfoEntity.getTaskId(), taskInfoEntity.getOrderId());
        taskInfoEntity.setOrderId(StringUtils.isBlank(orderInfoDTO.getOrderId()) ? taskInfoEntity.getOrderId() : orderInfoDTO.getOrderId());
        taskInfoEntity.setPolicyId(StringUtils.isBlank(orderInfoDTO.getInsuranceNo()) ? taskInfoEntity.getPolicyId() : orderInfoDTO.getInsuranceNo());
        taskInfoEntity.setProductCode(StringUtils.isBlank(orderInfoDTO.getProductCode()) ? taskInfoEntity.getProductCode() : orderInfoDTO.getProductCode());
        taskInfoEntity.setProductName(StringUtils.isBlank(orderInfoDTO.getProductName()) ? taskInfoEntity.getProductName() : orderInfoDTO.getProductName());
        taskInfoEntity.setApplicantName(StringUtils.isBlank(orderInfoDTO.getPolicyHolder()) ? taskInfoEntity.getApplicantName() : orderInfoDTO.getPolicyHolder());
        taskInfoEntity.setPlatform(StringUtils.isBlank(orderInfoDTO.getPlatform()) ? taskInfoEntity.getPlatform() : orderInfoDTO.getPlatform());
        taskInfoEntity.setAccount(StringUtils.isBlank(orderInfoDTO.getAccount()) ? taskInfoEntity.getAccount() : orderInfoDTO.getAccount());
        taskInfoEntity.setChannel(StringUtils.isBlank(orderInfoDTO.getChannel()) ? taskInfoEntity.getChannel() : orderInfoDTO.getChannel());
        taskInfoEntity.setExtraInfo(StringUtils.isBlank(orderInfoDTO.getExtraInfo()) ? taskInfoEntity.getExtraInfo() : orderInfoDTO.getExtraInfo());
        taskInfoEntity.setSuccess(Objects.isNull(orderInfoDTO.getSuccess()) ? taskInfoEntity.getSuccess() : orderInfoDTO.getSuccess());
        taskInfoEntity.setBusinessType(StringUtils.isBlank(orderInfoDTO.getBusinessType()) ? taskInfoEntity.getBusinessType() : orderInfoDTO.getBusinessType());
        taskInfoEntity.setAgencyCode(StringUtils.isBlank(orderInfoDTO.getAgencyCode()) ? taskInfoEntity.getAgencyCode() : orderInfoDTO.getAgencyCode());
        taskInfoEntity.setAgencyName(StringUtils.isBlank(orderInfoDTO.getAgencyName()) ? taskInfoEntity.getAgencyName() : orderInfoDTO.getAgencyName());
    }

    /**
     * ??????taskInfo????????????????????????
     *
     * @param taskInfoEntity
     * @param orderInfoDTO
     * @return
     * @author Logan
     * @date 2020-10-15 14:55
     */
    private void copyTaskFieldNullInDB(TaskInfoEntity taskInfoEntity, OrderInfoDTO orderInfoDTO) {

        if (StringUtils.isEmpty(taskInfoEntity.getOrderId())) {
            taskInfoEntity.setOrderId(orderInfoDTO.getOrderId());
        }
        if (StringUtils.isEmpty(taskInfoEntity.getPolicyId())) {
            taskInfoEntity.setPolicyId(orderInfoDTO.getInsuranceNo());
        }
        if (StringUtils.isEmpty(taskInfoEntity.getProductName())) {
            taskInfoEntity.setProductName(orderInfoDTO.getProductName());
        }
        if (StringUtils.isEmpty(taskInfoEntity.getProductCode())) {
            taskInfoEntity.setProductCode(orderInfoDTO.getProductCode());
        }
        if (StringUtils.isEmpty(taskInfoEntity.getApplicantName())) {
            taskInfoEntity.setApplicantName(orderInfoDTO.getPolicyHolder());
        }
        if (StringUtils.isEmpty(taskInfoEntity.getPlatform())) {
            taskInfoEntity.setPlatform(orderInfoDTO.getPlatform());
        }
        if (StringUtils.isEmpty(taskInfoEntity.getAccount())) {
            taskInfoEntity.setAccount(orderInfoDTO.getAccount());
        }
        if (StringUtils.isEmpty(taskInfoEntity.getChannel())) {
            taskInfoEntity.setChannel(orderInfoDTO.getChannel());
        }
        if (StringUtils.isEmpty(taskInfoEntity.getExtraInfo())) {
            taskInfoEntity.setExtraInfo(orderInfoDTO.getExtraInfo());
        }
        if (taskInfoEntity.getSuccess() == null) {
            taskInfoEntity.setSuccess(orderInfoDTO.getSuccess());
        }
        if (StringUtils.isEmpty(taskInfoEntity.getBusinessType())) {
            taskInfoEntity.setBusinessType(orderInfoDTO.getBusinessType());
        }
        if (StringUtils.isEmpty(taskInfoEntity.getAgencyCode())) {
            taskInfoEntity.setAgencyCode(orderInfoDTO.getAgencyCode());
        }
        if (StringUtils.isEmpty(taskInfoEntity.getAgencyName())) {
            taskInfoEntity.setAgencyName(orderInfoDTO.getAgencyName());
        }
    }

    /**
     * ??????OrderInfo??????
     *
     * @param orderInfoDTO    ??????????????????
     * @param orderInfoEntity ??????????????????
     * @param updateTimeStamp ???????????????????????????
     * @param versionId       ?????????
     * @return
     * @author Logan
     * @date 2020-10-15 11:59
     */
    private void copyOrderInfoDTO(OrderInfoDTO orderInfoDTO, OrderInfoEntity orderInfoEntity, Timestamp updateTimeStamp, Integer versionId) {
        if (orderInfoDTO != null && orderInfoEntity != null) {
            orderInfoEntity.setOrderId(orderInfoDTO.getOrderId());
            orderInfoEntity.setSuccess(orderInfoDTO.getSuccess());
            // ????????? ??????-??????-????????????????????????,??????????????????
            if (storageVersionType.name().equals(StorageVersionType.PRODUCT_GRANULARITY_VERSION_TYPE.name())) {
                orderInfoEntity.setProductGranularityVersionId(versionId);
            } else {
                // ????????? ????????????????????????????????????
                orderInfoEntity.setVersionId(versionId);
            }
            copyOrderInfoDTONullInDB(orderInfoDTO, orderInfoEntity);
            if (updateTimeStamp == null) {
                orderInfoEntity.setUploadTime(updateTimeStamp);
                copyOrderInfoDTOReplaceDB(orderInfoDTO, orderInfoEntity);
            } else if (orderInfoEntity.getUploadTime().before(updateTimeStamp) || orderInfoEntity.getUploadTime().equals(updateTimeStamp)) {
                orderInfoEntity.setUploadTime(updateTimeStamp);
                copyOrderInfoDTOReplaceDB(orderInfoDTO, orderInfoEntity);
            }
        }
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param orderInfoDTO
     * @param orderInfoEntity
     * @return
     * @author Logan
     * @date 2020-10-15 13:58
     */
    private void copyOrderInfoDTONullInDB(OrderInfoDTO orderInfoDTO, OrderInfoEntity orderInfoEntity) {
        if (StringUtils.isEmpty(orderInfoEntity.getAccount())) {
            orderInfoEntity.setAccount(orderInfoDTO.getAccount());
        }
        if (StringUtils.isEmpty(orderInfoEntity.getAgencyCode())) {
            orderInfoEntity.setAgencyCode(orderInfoDTO.getAgencyCode());
        }
        if (StringUtils.isEmpty(orderInfoEntity.getAgencyName())) {
            orderInfoEntity.setAgencyName(orderInfoDTO.getAgencyName());
        }
        if (StringUtils.isEmpty(orderInfoEntity.getApplicantName())) {
            orderInfoEntity.setApplicantName(orderInfoDTO.getPolicyHolder());
        }
        if (StringUtils.isEmpty(orderInfoEntity.getBusinessType())) {
            orderInfoEntity.setBusinessType(orderInfoDTO.getBusinessType());
        }
        if (StringUtils.isEmpty(orderInfoEntity.getChannel())) {
            orderInfoEntity.setChannel(orderInfoDTO.getChannel());
        }
        if (StringUtils.isEmpty(orderInfoEntity.getExtraInfo())) {
            orderInfoEntity.setExtraInfo(orderInfoDTO.getExtraInfo());
        }
        if (StringUtils.isEmpty(orderInfoEntity.getOrderId())) {
            orderInfoEntity.setOrderId(orderInfoDTO.getOrderId());
        }
        if (StringUtils.isEmpty(orderInfoEntity.getPlatform())) {
            orderInfoEntity.setPlatform(orderInfoDTO.getPlatform());
        }
        if (StringUtils.isEmpty(orderInfoEntity.getPolicyId())) {
            orderInfoEntity.setPolicyId(orderInfoDTO.getInsuranceNo());
        }
        if (StringUtils.isEmpty(orderInfoEntity.getProductCode())) {
            orderInfoEntity.setProductCode(orderInfoDTO.getProductCode());
        }
        if (StringUtils.isEmpty(orderInfoEntity.getProductName())) {
            orderInfoEntity.setProductName(orderInfoDTO.getProductName());
        }
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param orderInfoDTO
     * @param orderInfoEntity
     * @return
     * @author Logan
     * @date 2020-10-15 13:58
     */
    private void copyOrderInfoDTOReplaceDB(OrderInfoDTO orderInfoDTO, OrderInfoEntity orderInfoEntity) {
        if (StringUtils.isNotEmpty(orderInfoDTO.getAccount())) {
            orderInfoEntity.setAccount(orderInfoDTO.getAccount());
        }
        if (StringUtils.isNotEmpty(orderInfoDTO.getAgencyCode())) {
            orderInfoEntity.setAgencyCode(orderInfoDTO.getAgencyCode());
        }
        if (StringUtils.isNotEmpty(orderInfoDTO.getAgencyName())) {
            orderInfoEntity.setAgencyName(orderInfoDTO.getAgencyName());
        }
        if (StringUtils.isNotEmpty(orderInfoDTO.getBusinessType())) {
            orderInfoEntity.setBusinessType(orderInfoDTO.getBusinessType());
        }
        if (StringUtils.isNotEmpty(orderInfoDTO.getChannel())) {
            orderInfoEntity.setChannel(orderInfoDTO.getChannel());
        }
        if (StringUtils.isNotEmpty(orderInfoDTO.getExtraInfo())) {
            orderInfoEntity.setExtraInfo(orderInfoDTO.getExtraInfo());
        }
        if (StringUtils.isNotEmpty(orderInfoDTO.getInsuranceNo())) {
            orderInfoEntity.setPolicyId(orderInfoDTO.getInsuranceNo());
        }
        if (StringUtils.isNotEmpty(orderInfoDTO.getPlatform())) {
            orderInfoEntity.setPlatform(orderInfoDTO.getPlatform());
        }
        if (StringUtils.isNotEmpty(orderInfoDTO.getPolicyHolder())) {
            orderInfoEntity.setApplicantName(orderInfoDTO.getPolicyHolder());
        }
        if (StringUtils.isNotEmpty(orderInfoDTO.getProductCode())) {
            orderInfoEntity.setProductCode(orderInfoDTO.getProductCode());
        }
        if (StringUtils.isNotEmpty(orderInfoDTO.getProductName())) {
            orderInfoEntity.setProductName(orderInfoDTO.getProductName());
        }
    }

    /**
     * ??????????????????
     *
     * @param productCode
     * @param platform
     * @param channel
     * @return
     */
    private Integer getVersionId(String productCode, String platform, String channel) {

        // ????????? ?????????????????????,??????????????????
        if (storageVersionType.name().equals(StorageVersionType.PRODUCT_GRANULARITY_VERSION_TYPE.name())) {
            // ??????????????????-??????-?????????????????????????????????
            OperationVersionInfoEntity versionInfoEntity = null;
//            OperationVersionInfoEntity versionInfoEntity = operationVersionInfoMapper.selectLastVersionByCondition(productCode, platform, channel);

            if (versionInfoEntity == null) {
                // ????????? ??????-??????-?????? ??????????????????????????????????????????????????????
//                OperationVersionInfoEntity defaultSystemVersion = operationVersionInfoMapper.selectLastSystemVersion();
                OperationVersionInfoEntity defaultSystemVersion = null;
//                return defaultSystemVersion.getId();
            }
//            return versionInfoEntity.getId();
            return null;

        }
        // ????????? ????????????????????????????????????
        else {
            OperationVersionBasicInfoEntity version = operationVersionBasicInfoService.findMaxVersion(platform);
//            return version.getId();
            return null;
        }

    }


    /**
     * ???????????????????????????????????????????????????
     *
     * @param recordInfoId oss??????????????????
     * @param productCode  ????????????
     * @param channel      ??????
     * @param platform     ??????
     */
    private void saveRecordInfoResourceCache(Integer recordInfoId, String productCode, String channel, String platform) {

        // ??????????????????
        RecordInfoResourceCacheTO recordInfoResourceCacheTO = new RecordInfoResourceCacheTO();
        // recordInfo??????id
        recordInfoResourceCacheTO.setResourceInfoId(recordInfoId);
        if (storageType.equals(StorageType.MYSQL_STORAGE)) {
            recordInfoResourceCacheTO.setIsOss(0);
        } else {
            recordInfoResourceCacheTO.setIsOss(1);
        }
        // ????????????
        //  ??????????????????????????????
        /*if (StringUtils.isBlank(productCode) || StringUtils.isBlank(platform) || StringUtils.isBlank(channel)) {
            // ?????????????????????????????? ????????????-??????-?????? ???????????????????????????????????????????????????
            OperationVersionInfoEntity versionInfoEntity = operationVersionInfoMapper.selectLastSystemVersion();
            recordInfoResourceCacheTO.setVersion(versionInfoEntity.getSystemVersion());
            recordInfoResourceCacheTO.setVersionType(1);// H5

        } else */
        if (storageType.name().equals(StorageVersionType.PRODUCT_GRANULARITY_VERSION_TYPE.name())) {
            // ???????????????-??????-???????????????????????????
            OperationVersionInfoEntity versionInfoEntity = null;
//            OperationVersionInfoEntity versionInfoEntity = operationVersionInfoMapper.selectLastVersionByCondition(productCode, platform, channel);
            if (versionInfoEntity == null) {
                // ????????????-??????-?????????????????????????????? ?????????????????????????????????
//                versionInfoEntity = operationVersionInfoMapper.selectLastSystemVersion();
            }
            recordInfoResourceCacheTO.setVersion(versionInfoEntity.getSystemVersion());
            recordInfoResourceCacheTO.setVersionType(1);// H5
        } else {
            // ???????????????????????????
            OperationVersionBasicInfoEntity infoServiceMaxVersion = operationVersionBasicInfoService.findMaxVersion(platform);
            if (infoServiceMaxVersion != null) {
                recordInfoResourceCacheTO.setVersion(infoServiceMaxVersion.getVersion());
                recordInfoResourceCacheTO.setVersionType(infoServiceMaxVersion.getVersionType());
            }
        }
        // ????????????
        recordInfoResourceCache.rightPushForList(recordInfoResourceCacheTO);
        log.info("redis????????????url??????????????? recordInfoId?????? {}", recordInfoId);
        log.info("recordInfoId---????????? = {}", recordInfoId);
    }

    /**
     * ??????????????????
     *
     * @param inputTimestamp
     * @return
     */
    private Timestamp getCreateTime(String inputTimestamp) {
        return DateUtils.getTimeStamp(inputTimestamp, true);
    }


/*    private Long getWebTimeStamp(String timeStampStr){
        LocalDateTime timeStamp = LocalDateTime.now();
        try{
            return Long.valueOf(timeStampStr);
        }catch (Exception e){
            ZoneId zone = ZoneId.systemDefault();
??????         Instant instant = localDateTime.atZone(zone).toInstant();
??????         return instant.toEpochMilli();
        }
    }*/

    private void recordInfoVersionProcess(Integer recordInfoId, String platform, Integer last, String taskId) {
        // ?????????????????????????????????????????????
        OperationVersionBasicInfoEntity infoServiceMaxVersion = operationVersionBasicInfoService.findMaxVersion(platform);
        if (infoServiceMaxVersion != null) {
//            Integer maxVersionId = infoServiceMaxVersion.getId();
            Integer maxVersionId = null;
            saveRecordInfoVersion(maxVersionId, recordInfoId, platform, last);
            log.info("recording ?????????????????????... taskId : {}, ?????????{}", taskId, maxVersionId);

        }
    }


    private Date timeMillsToDate(String timestamp) {
        Long timeStampLong = Long.parseLong(timestamp);
        Date mydate = new Date();
        mydate.setTime(timeStampLong);
        return mydate;
    }

    /**
     * ??????recordInfo?????????????????????
     *
     * @param maxVersionId
     * @param recordInfoId
     */
    private void saveRecordInfoVersion(Integer maxVersionId, Integer recordInfoId, String platform, Integer last) {
        RecordInfoExtEntity recordInfoExt = new RecordInfoExtEntity();
        recordInfoExt.setRid(recordInfoId);
        recordInfoExt.setVersionId(maxVersionId);
        recordInfoExt.setPlatform(platform);
        recordInfoExt.setLast(last);
//        recordInfoExtDao.save(recordInfoExt);

    }

    /**
     * recordInfo????????????
     *
     * @param recordInfoTaskBO
     * @return
     */
    public ResourceProcessResultBO recordInfoResourceProcess(RecordInfoTaskBO recordInfoTaskBO, String version) {
        String recordInfo = recordInfoTaskBO.getRecordInfo();
        Integer resourceInfoId = recordInfoTaskBO.getResourceInfoId();
        String bucketName = recordInfoTaskBO.getBucketName();
        Integer isOss = recordInfoTaskBO.getIsOss();
        Integer encodeModeValue = null;
//        Integer encodeModeValue = taskInfoDao.getEncodeMode(recordInfoTaskBO.getTaskId());
        EncodeMode encodeMode = EncodeMode.getEncodeMode(encodeModeValue);
        // ????????????
        String decrypt = decode(recordInfo, resourceInfoId, recordInfoTaskBO.getOrderId(), recordInfoTaskBO.getTaskId(), encodeMode);
        if (StringUtils.isBlank(decrypt)) {
            log.info("???????????????recordInfo?????????????????????????????????????????????recordInfo??????????????????????????????... url??????????????????...");
            return processResult(recordInfo, RESOURCE_DOWNLOAD_STATUS_REPLACED_FAILURE);
        }
        // ??????
        String localFilePathProfix = getLocalFilePathProfix(version, isOss);
        String process = recordInfoResolverHandler.process(decrypt, resourceInfoId, bucketName, localFilePathProfix, version);
        log.info("recordInfoId---????????? = {}", resourceInfoId);

        if (StringUtils.isBlank(process)) {
            log.info("???????????????????????????url?????????????????????recordInfo??????????????????????????????...????????????????????????...");
            return processResult(recordInfo, RESOURCE_DOWNLOAD_STATUS_REPLACED_FAILURE);
        }
        String encrypt = encode(encodeMode, process);
        if (StringUtils.isBlank(encrypt)) {
            log.info("???????????????recordInfo?????????????????????????????????????????????recordInfo??????????????????????????????... ????????????????????????...");
            return processResult(recordInfo, RESOURCE_DOWNLOAD_STATUS_REPLACED_FAILURE);
        }
        return processResult(encrypt, RESOURCE_DOWNLOAD_STATUS_REPLACED_SUCCESS);
    }

    private ResourceProcessResultBO processResult(String recordInfo, Integer downloadStatus) {
        ResourceProcessResultBO resourceProcessResultBO = new ResourceProcessResultBO();
        resourceProcessResultBO.setDownLoadStatus(downloadStatus);
        resourceProcessResultBO.setEncryptRecordInfoProcessResult(recordInfo);
        return resourceProcessResultBO;
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    private String getLocalFilePathProfix(String version, Integer isOss) {
        if (isOss == 1) {
            // oss??????
            String ossPathPrefix = CommonContent.RESOURCE_OSS_PREFIX + "/" + CommonContent.RECORD_INFO_PATH;
            return String.format("%s/version_%s/%s/%s", ossPathPrefix, version, DateUtils.dateConvertStr(), StUtils.getUUID());
        } else {
            // mysql??????
            return String.format("%s/version_%s/%s/%s", CommonContent.RECORD_INFO_UPLOAD_PATH, version, DateUtils.dateConvertStr(), StUtils.getUUID());
        }
    }


    private List<String> urlFileFilter(List<String> urls) {
        List<String> list = new ArrayList<>();
        for (String url : urls) {
            int index = url.lastIndexOf("/");
            if (index != -1) {
                String urlTail = url.substring(index);
                if (!urlTail.contains(".")) {
                    list.add(url);
                }
            }
        }
        return list;

    }


    private List<String> strUrlResolve(String recordInfo) {
        List<String> list = new ArrayList<>();
        // ????????????
        String decrypt = aesDecrypt(recordInfo);
        // ??????http???????????????(??????)
        String[] hrefs = decrypt.split("http");
        // ???????????????url??????
        for (int i = 0; i < hrefs.length; i++) {
            String href = hrefs[i];
            // ????????????????????????
            String s = "http" + href;
            // ???????????????????????????????????? StackOverflowError -> ??????????????????????????????????????????
            if (s.length() > 300) {
                s = s.substring(0, 300);
            }
            String matchUrl = RegExUtils.matchUrl(s);
            list.add(matchUrl);
        }
        return list;
    }

    /**
     * ??????(????????????)
     *
     * @param encryptStr
     * @return
     */
    private String aesDecrypt(String encryptStr) {
        try {
            return jsaesUtils.aesDecode(encryptStr);
        } catch (Exception exception) {
            log.info("AES????????????");
            exception.printStackTrace();
            return "";
        }
    }

    /**
     * ??????(????????????) JS???
     *
     * @param encryptStr
     * @param orderId
     * @param taskId
     * @return
     */
    private String decode(String encryptStr, Integer resourceInfoId, String orderId, String taskId, EncodeMode encodeMode) {

        String result = "";
        try {
            result = decode(encodeMode, encryptStr);
        } catch (Exception e) {
            log.error("JSAESDecrypt ???????????????????????? orderId:{},  taskId : {} ", orderId, taskId, e);
            saveDecryptErrorMethod(resourceInfoId, orderId, taskId);
        }
        return result;

    }

    /**
     * ???recordInfo?????????????????????????????????Id
     *
     * @param rid
     */
    private void saveDecryptErrorMethod(Integer rid, String orderId, String taskId) {
        RecordInfoDecryptErrorBO recordInfoDecryptErrorBO = new RecordInfoDecryptErrorBO();
        recordInfoDecryptErrorBO.setRid(rid);
        recordInfoDecryptErrorBO.setOrderId(orderId);
        recordInfoDecryptErrorBO.setTaskId(taskId);
        recordInfoDecryptErrorService.save(recordInfoDecryptErrorBO);
        log.info("recording AES Decrypt ????????????????????? ??????id?????????...");

    }

    private String decode(EncodeMode encodeMode, String encryptStr) throws ScriptException, NoSuchMethodException {
        switch (encodeMode) {
            case ENCODE_AES:
                return jsaesUtils.aesDecode(encryptStr);
            case ENCODE_PLAINTEXT:
                return encryptStr;
            case ENCODE_COMPRESS:   //TODO:  to be implement  Logan  2020-11-10
            default:
                return "";// do nothing;
        }
    }

    /**
     * ??????(????????????)
     *
     * @param encodeMode
     * @param decryptStr
     * @return
     */
    private String encode(EncodeMode encodeMode, String decryptStr) {
        try {
            switch (encodeMode) {
                case ENCODE_AES:
                    return jsaesUtils.aesEncode(decryptStr);
                case ENCODE_PLAINTEXT:
                    return decryptStr;
                case ENCODE_COMPRESS: //TODO:  to be implemented   Logan  2020-11-10
                default:
                    return decryptStr;// do nothing
            }

        } catch (Exception exception) {
            log.info("AES????????????");
            exception.printStackTrace();
            return "";
        }
    }

    public Result<Object> getOrderRecordList(OrderRecordListVO orderRecordListVO) {
        if (null == SituThreadLocal.getUserInfo()) {
            throw new ResultException(RespCode.ERROR_LOGIN_NOROLE, "");
        }
        UserInfoCacheBO userInfo = SituThreadLocal.getUserInfo();
        if (null != userInfo.getId() && userInfo.getId() != 1) {
            // ???????????????????????????????????????????????????????????????
            if (StringUtils.isNotBlank(userInfo.getChannel())) {
                String[] channelArray = userInfo.getChannel().split(",");
                List<String> channelList = Arrays.asList(channelArray);
                orderRecordListVO.setChannelList(channelList);
            }
            if (StringUtils.isNotBlank(userInfo.getBusinessType())) {
                String[] businessTypeArray = userInfo.getBusinessType().split(",");
                List<String> businessTypelist = Arrays.asList(businessTypeArray);
                orderRecordListVO.setBusinessTypeList(businessTypelist);
            }
            // ???????????????????????????????????????
            List<String> userAgencyByCode = null;
//            List<String> userAgencyByCode = agencyDao.findUserAgencyByCode(userInfo.getId());
            orderRecordListVO.setAgencyList(userAgencyByCode);
        }
        Integer pageNumber = orderRecordListVO.getPerPage();
        Integer currPage = orderRecordListVO.getCurrPage();
        Page<List<RecordOrderInfoVO>> orderInfoEntityPage = new Page<>();
        orderInfoEntityPage.setCurrent(currPage);
        orderInfoEntityPage.setSize(pageNumber);
        if (Objects.nonNull(orderRecordListVO.getUploadStartTime())) {
            orderRecordListVO.setUploadStartTime(DateUtils.getStartTimeInDateOf(orderRecordListVO.uploadStartTime));
        }
        if (Objects.nonNull(orderRecordListVO.getUploadEndTime())) {
            orderRecordListVO.setUploadEndTime(DateUtils.getEndTimeInDateOf(orderRecordListVO.uploadEndTime));
        }

        IPage<RecordOrderInfoVO> listIPage = null;
        if (orderRecordListVO.getCateOrder() == 1) {
            //??????taskId??????orderId
            if (StringUtils.isNotEmpty(orderRecordListVO.getTaskId())) {
                if (taskIdFilterConflictOrderIdFilter(orderRecordListVO)) {
                    //????????????????????????????????????????????????????????????????????????,?????????????????????orderId????????????????????? ???????????????
                    List<OrderRecordListVO> data = new ArrayList<>(0);
                    return Result.success(PageBO.handler(data, currPage, 0, pageNumber));
                }
            }
            listIPage = null;
//            listIPage = orderInfoMapper.selectOrderInfoListPage(orderInfoEntityPage, orderRecordListVO);
            //??????????????????
            setLatestTaskId(listIPage);
        } else {
//            listIPage = orderInfoMapper.selectNoOrderInfoListPage(orderInfoEntityPage, orderRecordListVO);
            listIPage = null;
        }
        PageBO pageBO = PageBO.handler(listIPage.getRecords(), currPage, (int) listIPage.getTotal(), pageNumber);
        return Result.success(pageBO);
    }

    /**
     * ?????????????????????????????????
     *
     * @param listIPage
     * @return
     * @author Logan
     * @date 2020-11-05 12:28
     */
    private void setLatestTaskId(IPage<RecordOrderInfoVO> listIPage) {
        if (listIPage.getRecords().size() > 0) {
            List<String> orderIdList = listIPage.getRecords().stream().map(RecordOrderInfoVO::getOrderId).parallel().collect(Collectors.toList());
            List<TaskInfoEntity> tasks = null;
//            List<TaskInfoEntity> tasks = taskInfoDao.findTaskIdsInOrderIds(orderIdList);
            Map<String, List<TaskInfoEntity>> orderTaskMaps = tasks.stream().sorted(
                    Comparator.comparing(TaskInfoEntity::getUploadTime)).parallel().collect(
                    Collectors.groupingBy(TaskInfoEntity::getOrderId));
            listIPage.getRecords().stream().forEach(vo -> {
                String orderId = vo.getOrderId();
                List<TaskInfoEntity> orderTaskList = orderTaskMaps.get(orderId);
                if (orderTaskList != null && orderTaskList.size() > 0) {
                    //????????????taskId
                    vo.setTaskId(orderTaskList.get(orderTaskList.size() - 1).getTaskId());
                }
            });
        }
    }

    /**
     * ????????????????????????????????????????????????????????????????????????,???????????????????????????orderId?????????????????????
     *
     * @param orderRecordListVO
     * @return
     * @author Logan
     * @date 2020-11-05 11:29
     */
    private Boolean taskIdFilterConflictOrderIdFilter(OrderRecordListVO orderRecordListVO) {
        if (StringUtils.isNotEmpty(orderRecordListVO.getTaskId())) {
//            String orderId = orderInfoMapper.findOrderIdByTaskId(orderRecordListVO.getTaskId());
            String orderId = null;
            if (StringUtils.isNotEmpty(orderId)) {
                //?????????orderId,????????????orderId??????
                if (StringUtils.isEmpty(orderRecordListVO.getOrderId())) {
                    //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    orderRecordListVO.setOrderId(orderId);
                } else {
                    if (!orderRecordListVO.getOrderId().equals(orderId)) {
                        //?????????????????????????????????????????????????????????????????????
                        return true;
                    }
                }
            } else {
                //??????taskId????????????orderId?????????????????????
                return true;
            }
        }
        return false;
    }

    /**
     * ???????????????Excel??????
     *
     * @param orderRecordListVO
     */
    public void orderRecordDownloadExcel(HttpServletResponse response, OrderRecordListVO orderRecordListVO) {
        if (orderRecordListVO.getCateOrder() != 1) {
            log.info("??????????????????????????????????????????????????????Excel??????...");
            easyExportExcel(response, new ArrayList<RecordOrderInfoExcelBO>());
            return;
        }
        if (null == SituThreadLocal.getUserInfo()) {
            throw new ResultException(RespCode.ERROR_LOGIN_NOROLE, "");
        }
        UserInfoCacheBO userInfo = SituThreadLocal.getUserInfo();
        if (null != userInfo.getId() && userInfo.getId() != 1) {
            // ???????????????????????????????????????????????????????????????
            if (StringUtils.isNotBlank(userInfo.getChannel())) {
                String[] channelArray = userInfo.getChannel().split(",");
                List<String> channelList = Arrays.asList(channelArray);
                orderRecordListVO.setChannelList(channelList);
            }
            if (StringUtils.isNotBlank(userInfo.getBusinessType())) {
                String[] businessTypeArray = userInfo.getBusinessType().split(",");
                List<String> businessTypelist = Arrays.asList(businessTypeArray);
                orderRecordListVO.setBusinessTypeList(businessTypelist);
            }
            // ???????????????????????????????????????
            List<String> userAgencyByCode = null;
//            List<String> userAgencyByCode = agencyDao.findUserAgencyByCode(userInfo.getId());
            orderRecordListVO.setAgencyList(userAgencyByCode);
        }
        if (Objects.nonNull(orderRecordListVO.getUploadStartTime())) {
            orderRecordListVO.setUploadStartTime(DateUtils.getStartTimeInDateOf(orderRecordListVO.uploadStartTime));
        }
        if (Objects.nonNull(orderRecordListVO.getUploadEndTime())) {
            orderRecordListVO.setUploadEndTime(DateUtils.getEndTimeInDateOf(orderRecordListVO.uploadEndTime));
        }
        //??????taskId??????orderId
        if (StringUtils.isNotEmpty(orderRecordListVO.getTaskId())) {
            if (taskIdFilterConflictOrderIdFilter(orderRecordListVO)) {
                //????????????????????????????????????????????????????????????????????????,?????????????????????orderId????????????????????? ???????????????
                easyExportExcel(response, new ArrayList<RecordOrderInfoExcelBO>());
                return;
            }
        }
        List<RecordOrderInfoExcelBO> recordOrderInfoExcelBOS = null;
//        List<RecordOrderInfoExcelBO> recordOrderInfoExcelBOS = orderInfoMapper.selectOrderInfoListPageWhole(orderRecordListVO);
        setLatestTaskId(recordOrderInfoExcelBOS);
        easyExportExcel(response, recordOrderInfoExcelBOS);
        log.info("???????????????Excel????????????...");
    }

    /**
     * ??????????????? ???????????????EasyExcel???
     *
     * @param response
     * @param orderInfoExcelBOS
     */
    public void easyExportExcel(HttpServletResponse response, List<RecordOrderInfoExcelBO> orderInfoExcelBOS) {

        try {
            EasyExcel.write(response.getOutputStream(), RecordOrderInfoExcelBO.class).sheet(RECORD_ORDER_EXCEL_SHEET).doWrite(orderInfoExcelBOS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setLatestTaskId(List<RecordOrderInfoExcelBO> listIPage) {
        if (listIPage.size() > 0) {
            List<String> orderIdList = listIPage.stream().map(RecordOrderInfoExcelBO::getOrderId).parallel().collect(Collectors.toList());
            List<TaskInfoEntity> tasks = null;
//            List<TaskInfoEntity> tasks = taskInfoDao.findTaskIdsInOrderIds(orderIdList);
            Map<String, List<TaskInfoEntity>> orderTaskMaps = tasks.stream().sorted(
                    Comparator.comparing(TaskInfoEntity::getUploadTime)).parallel().collect(
                    Collectors.groupingBy(TaskInfoEntity::getOrderId));
            listIPage.stream().forEach(vo -> {
                String orderId = vo.getOrderId();
                List<TaskInfoEntity> orderTaskList = orderTaskMaps.get(orderId);
                if (orderTaskList != null && orderTaskList.size() > 0) {
                    //????????????taskId
                    vo.setTaskId(orderTaskList.get(orderTaskList.size() - 1).getTaskId());
                }
            });
        }
    }

    /**
     * ??????????????????taskId
     *
     * @param orderId
     */
    public Result<Object> getOrderTaskList(String orderId) {

        List<String> taskIdList = null;
//        List<String> taskIdList = recordInfoDao.findTaskIdListByOrderId(orderId);

        return Result.success(taskIdList);
    }

    /**
     * ????????????id??????recordInfo????????????
     *
     * @param id
     * @return
     */
    public RecordInfoEntity getRecordInfoById(Integer id) {

        return null;
//        return recordInfoDao.findRecordInfoById(id);
    }

    /**
     * ????????????/?????????????????????
     *
     * @param recordResourceDTO
     * @return
     */
    public Result saveResource(RecordResourceDTO recordResourceDTO) {
        ImageResourceDTO[] image = recordResourceDTO.getImage();
        // ?????????????????????
        String taskId = recordResourceDTO.getTaskId();
        List<RecordResourceEntity> recordResourceEntities = recordImageResource(image, taskId);
        // TODO ????????????????????????????????? ????????????????????????????????????????????????????????????????????????????????????????????????
        if (null != recordResourceDTO.getVideo() && recordResourceDTO.getVideo().length > 0) {
            List<RecordResourceEntity> resourceVideoEntities = recordVideoResource(recordResourceDTO.getVideo(), taskId);
            recordResourceEntities.addAll(resourceVideoEntities);
        }
        for (RecordResourceEntity recordResourceEntity : recordResourceEntities) {
//            recordSourceDao.saveResource(recordResourceEntity);
        }
        log.info("??????????????????????????????...");
        return Result.success(RespCode.SUCC);
    }

    /**
     * ??????image??????
     *
     * @param imageResourceDTOS
     * @param taskId
     * @return
     */
    private List<RecordResourceEntity> recordImageResource(ImageResourceDTO[] imageResourceDTOS, String taskId) {
        List<RecordResourceEntity> resourceEntities = new ArrayList<>();
        for (ImageResourceDTO recordResourceDTO : imageResourceDTOS) {
            String imageTitle = recordResourceDTO.getTitle();
            // ??????images
            List<RecordResourceEntity> imageResource = Arrays.stream(recordResourceDTO.getImageList()).map(imageResourceInfoDTO ->
                    instance(taskId, imageTitle, imageResourceInfoDTO.getEncode(), imageResourceInfoDTO.getName(), CommonContent.IMAGE))
                    .collect(Collectors.toList());
            resourceEntities.addAll(imageResource);
        }
        return resourceEntities;

    }

    /**
     * ?????? RecordResourceEntity??????
     *
     * @param taskId
     * @param title
     * @param source
     * @param sourceName
     * @param sourceType
     * @return
     */
    private RecordResourceEntity instance(String taskId, String title, String source, String sourceName, String sourceType) {
        return new RecordResourceEntity()
                /*.setTaskId(taskId)
                .setTitle(title)
                .setResource(source)
                .setResourceName(sourceName)
                .setResourceType(sourceType)
                .setCreateTime(new Timestamp(System.currentTimeMillis()))*/;

    }

    /**
     * ?????? video ??????
     *
     * @param videoResourceDTOS
     * @param taskId
     * @return
     */
    private List<RecordResourceEntity> recordVideoResource(VideoResourceDTO[] videoResourceDTOS, String taskId) {
        List<RecordResourceEntity> resourceEntities = new ArrayList<>();
        for (VideoResourceDTO videoResourceDTO : videoResourceDTOS) {
            String videoTitle = videoResourceDTO.getTitle();
            // ??????video
            List<RecordResourceEntity> videoResource = Arrays.stream(videoResourceDTO.getVideoList()).map(videoResourceInfoDTO ->
                    instance(taskId, videoTitle, videoResourceInfoDTO.getPath(), videoResourceInfoDTO.getName(), CommonContent.VIDEO))
                    .collect(Collectors.toList());
            resourceEntities.addAll(videoResource);
        }
        return resourceEntities;
    }


    public List<String> findRecordInfoByOrderIdAndTaskId(String orderId, String taskId) {
        return null;
//        return recordInfoDao.findRecordInfoByOrderIdAndTaskId(orderId, taskId);
    }


    public Boolean emptyEvent(String recordInfo) {
        try {
            String emptyEventEncrypt = jsaesUtils.aesEncode("[]");
            if (emptyEventEncrypt.equals(recordInfo)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("???????????????????????????????????????????????????", e);
            return false;
        }

    }


    public Result resourceImage(ResourceImageDTO recordResourceDTO) {
        String productCode = recordResourceDTO.getProductCode();
        Timestamp createTime = new Timestamp(recordResourceDTO.getCreateTime());
        OperationVersionAppletProductEntity appletProductEntities = null;
//        OperationVersionAppletProductEntity appletProductEntities = appletProductDao.findAllByProductCode(productCode);
        if (null == appletProductEntities) {
            return Result.success(RespCode.ERROR_APPLET_PRODUCT_EMPTY.getMsg());
        }
        Integer productId = appletProductEntities.getId();
        List<OperationVersionBasicInfoEntity> versionBasicInfoEntities = operationVersionBasicInfoService.findAllAppletByMoreCreateTime(productId, createTime);
        if (null == versionBasicInfoEntities || versionBasicInfoEntities.size() == 0) {
            return Result.success(RespCode.ERROR_APPLET_VERSION_EMPTY.getMsg());
        }
        OperationVersionBasicInfoEntity versionBasicInfoEntity = versionBasicInfoEntities.stream().findFirst().get();
        Integer appletVersionId = versionBasicInfoEntity.getId();

//        List<String> imagePaths = appletImageDao.findImageByVersionIdAndProductId(appletVersionId, productId);
        List<String> imagePaths = null;
        if (null == imagePaths || imagePaths.size() == 0) {
            return Result.success(RespCode.ERROR_APPLET_VERSION_IMAGE_EMPTY.getMsg());
        }
        List<String> images = imagePaths.stream().map(item -> CommonContent.DO_MAIN + item).collect(Collectors.toList());
        return Result.success(images);
    }

    /**
     * ????????????id??????recordInfo???????????? ?????????????????????
     *
     * @param id
     */
    public void updateRecordInfoAndStatusById(Integer id, String recordInfo, Integer downloadStatus) {
//        recordInfoDao.updateRecordInfoAndStatusById(id, recordInfo, downloadStatus);
    }

    public void updateDownloadStatusById(Integer id, Integer downloadStatus) {
//        recordInfoDao.updateDownloadStatusById(id, downloadStatus);
    }

    public List<Integer> findIdByTaskIdAndOrderId(List<String> taskIds) {
        return null;
//        return recordInfoDao.findIdByTaskIdAndOrderId(taskIds);
    }


    public void recordDetailImageResourcePack(HttpServletResponse response, String taskId) {
        List<RecordResourceEntity> recordResourceEntities = null;
//        List<RecordResourceEntity> recordResourceEntities = recordSourceDao.findImageByTaskId(taskId);
        if (recordResourceEntities == null || recordResourceEntities.size() == 0) {
            log.warn("???????????????????????????????????????zip??????????????????taskId????????????????????????????????? taskId:{}", taskId);
            return;
        }
        List<RecordResourceEntity> recordResource = recordResourceRename(recordResourceEntities);
        ZipOutputStream zos = null;
        try {
            log.info("???????????????????????????????????????zip????????????... ?????????taskId???{}", taskId);
            zos = new ZipOutputStream(response.getOutputStream());

            for (RecordResourceEntity recordResourceEntity : recordResource) {
                String imageResource = recordResourceEntity.getResource();
                // ??????img??????????????????
                String base64FormalSuffix = ImageUtils.getBase64FormalSuffix(imageResource);
                String imageFormatSuffix = String.format(".%s", base64FormalSuffix);
                log.info("image ????????????:" + base64FormalSuffix);
                // ??????base64?????????
                String imgBase64 = imageResource;
                int index = imageResource.indexOf(BASE64);
                if (index != -1) {
                    imgBase64 = imageResource.substring(index + BASE64.length());
                }
                // ????????????zip
                InputStream inputStream = ImageUtils.BaseToInputStream(imgBase64);
                ZipEntry zipEntry = new ZipEntry(recordResourceEntity.getResourceName() + imageFormatSuffix);
                zos.putNextEntry(zipEntry);
                // ????????? ???GIF?????????????????????????????????
//                ImageIO.write(bufferedImage, base64FormalSuffix, zos);
                //????????????????????????
                byte[] buffer = new byte[1024];
                int readLenghth;
                while ((readLenghth = inputStream.read(buffer, 0, 1024)) != -1) {//????????????????????????buffer?????????
                    zos.write(buffer, 0, readLenghth);//??????buffer???????????????????????????
                }
                inputStream.close();
            }

            log.info("???????????????????????????????????????zip????????????...");
        } catch (IOException e) {
            log.error("???????????????????????????????????????zip????????????...", e);
        } finally {
            try {
                zos.finish();
                zos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * ????????????????????????????????????????????????????????????
     *
     * @param list
     * @return
     */
    public List<RecordResourceEntity> recordResourceRename(List<RecordResourceEntity> list) {
        List<RecordResourceEntity> result = new ArrayList<>();
        // ??????????????????????????????
        Map<String, List<RecordResourceEntity>> groupByResourceNameList = list.stream().collect(Collectors.groupingBy(RecordResourceEntity::getResourceName));
        // ??????????????????
        Set<Map.Entry<String, List<RecordResourceEntity>>> entries = groupByResourceNameList.entrySet();
        for (Map.Entry<String, List<RecordResourceEntity>> entry : entries) {
            List<RecordResourceEntity> entryValue = entry.getValue();
            // ???????????????????????????????????? -> ???????????????????????????????????? ???????????????1??????????????????2
            if (entryValue.size() > 1) {
                for (int i = 0; i < entryValue.size(); i++) {
                    RecordResourceEntity recordResourceEntity = entryValue.get(i);
                    String resourceName = recordResourceEntity.getResourceName();
                    String realResourceName = resourceName + "???" + (i + 1);
                    recordResourceEntity.setResourceName(realResourceName);
                    result.add(recordResourceEntity);
                }

            } else {
                result.addAll(entryValue);
            }

        }
        return result;
    }

    public void recordDetailConsumerResourceDownload(HttpServletResponse response, ConsumerResourceExcelDTO resourceExcelDTO) {
        // ?????????????????? ????????????????????????????????????map??????
//        List<ConsumerResourceExcelDTO.RichTextDTO> richText = resourceExcelDTO.getRichTextList();
//        Map<Integer, String> richTextMap = richText.stream().collect(Collectors.toMap(ConsumerResourceExcelDTO.RichTextDTO::getId, ConsumerResourceExcelDTO.RichTextDTO::getBase64));
        List<CustomerServiceInfoEntity> customerServiceInfoEntities = returnInfoService.findAllByTaskId(resourceExcelDTO.getTaskId());
        // ???????????????????????????????????? String??? ???????????????inputStream??????
        // ?????????ExcelBO
        List<ConsumerResourceExcelBO> consumerResourceExcelBOS = toResourceExcelBOConverter(customerServiceInfoEntities);
        // ??????response excel
        try {
            EasyExcel.write(response.getOutputStream(), ConsumerResourceExcelBO.class).sheet(RECORD_ORDER_EXCEL_SHEET).doWrite(consumerResourceExcelBOS);
            log.info("??????????????????????????? download Excel ????????????...");
        } catch (IOException e) {
            log.info("??????????????????????????? download Excel??? ???easyExcel???????????????response????????????????????????", e);
        }
    }


    /**
     * DB??????????????????ExcelBO????????????excel?????????
     *
     * @param serviceInfoEntities
     * @return
     */
    public List<ConsumerResourceExcelBO> toResourceExcelBOConverter(List<CustomerServiceInfoEntity> serviceInfoEntities) {
        List<ConsumerResourceExcelBO> consumerResourceExcelBOS = serviceInfoEntities.stream().map(customerServiceInfoEntity -> {
            ConsumerResourceExcelBO consumerResourceExcelBO = new ConsumerResourceExcelBO();
            ConsumerResource.RoleTypeEnum roleTypeEnum = ConsumerResource.RoleTypeEnum.valueOfByCode(Integer.parseInt(customerServiceInfoEntity.getPersonType()));
            // ????????????
            if (roleTypeEnum != null) {
                consumerResourceExcelBO.setRole(roleTypeEnum.getRole());
            }
            // ????????????
            Date date = DateUtils.timestampToDate(customerServiceInfoEntity.getCreateTime());
            consumerResourceExcelBO.setCreateTime(date);

            String contextType = customerServiceInfoEntity.getContextType();
            // ?????????????????????????????????????????????
            if (customerServiceInfoEntity.getInstead() != null && customerServiceInfoEntity.getInstead() == 1) {
                if (ConsumerResource.ContextTypeEnum.IMAGE.getCode().toString().equals(contextType)) {
                    // ????????????
                    String contextInfo = returnInfoService.getContextInfoSignatureUrl(customerServiceInfoEntity.getInsteadContext(), contextType);
                    try {
                        InputStream inputStream = URLUtils.url2InputStream(contextInfo);
                        consumerResourceExcelBO.setImageContext(inputStream);
                    } catch (Exception e) {
                        log.error("????????????Download Excel, easyExcel??????imageContext??????????????????????????????", e);
                    }
                } else {
                    String contextInfo = returnInfoService.getContextInfo(customerServiceInfoEntity.getInsteadContext(), contextType);
                    consumerResourceExcelBO.setTextContext(contextInfo);
                }


            } else {
                // ???mysql????????????????????????
                String context = customerServiceInfoEntity.getContext();
                // ??????
                if (ConsumerResource.ContextTypeEnum.TEXT.getCode().toString().equals(contextType)) {
                    consumerResourceExcelBO.setTextContext(context);
                }
                // ??????
                if (ConsumerResource.ContextTypeEnum.IMAGE.getCode().toString().equals(contextType)) {
                    InputStream inputStream = ImageUtils.BaseToInputStream(context);
                    consumerResourceExcelBO.setImageContext(inputStream);
                }
                // ??????
                if (ConsumerResource.ContextTypeEnum.LINK.getCode().toString().equals(contextType)) {
                    consumerResourceExcelBO.setTextContext(context);
                }
                // ?????????
                if (ConsumerResource.ContextTypeEnum.RICHTEXT.getCode().toString().equals(contextType)) {
                    consumerResourceExcelBO.setTextContext(context);
                }
            }
            return consumerResourceExcelBO;
        }).collect(Collectors.toList());

        return consumerResourceExcelBOS;
    }

    public List<Integer> findRecordOssIdByTaskId(List<String> taskIds) {
        return null;
//        return recordInfoDao.findRecordOssIdByTaskId(taskIds);

    }

}