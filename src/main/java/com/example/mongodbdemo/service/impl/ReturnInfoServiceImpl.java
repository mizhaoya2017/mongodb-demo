package com.example.mongodbdemo.service.impl;

import com.example.mongodbdemo.content.CommonContent;
import com.example.mongodbdemo.data.dto.CustomerServiceInfoDTO;
import com.example.mongodbdemo.data.dto.GetTaskDetailDTO;
import com.example.mongodbdemo.data.dto.OrderInfoDTO;
import com.example.mongodbdemo.data.dto.RecordInfoDTO;
import com.example.mongodbdemo.data.vo.CustomerServiceInfoVO;
import com.example.mongodbdemo.data.vo.GetTaskDetailVO;
import com.example.mongodbdemo.data.vo.RespCode;
import com.example.mongodbdemo.data.vo.Result;
import com.example.mongodbdemo.entity.CustomerServiceInfoEntity;
import com.example.mongodbdemo.enums.StorageType;
import com.example.mongodbdemo.oss.service.OSSService;
import com.example.mongodbdemo.service.*;
import com.example.mongodbdemo.utils.DateUtils;
import com.example.mongodbdemo.utils.ImageUtils;
import com.example.mongodbdemo.utils.StUtils;
import com.example.mongodbdemo.utils.URLUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author xieyitong  <xieyitong@situdata.com>
 * @Date 2020/9/7 16:31
 **/
@Service
@Slf4j
public class ReturnInfoServiceImpl implements ReturnInfoService, Serializable {


//    @Autowired
//    private ReturnInfoServiceDao returnInfoServiceDao;
//    @Autowired
//    private RecordInfoDao recordInfoDao;
    @Autowired
    private FileService fileService;
//    @Autowired
//    private TaskInfoDao taskInfoDao;
    @Autowired
    private RecordInfoMultiStorageService recordInfoMultiStorageService;
    @Autowired
    private RecordInfoService recordInfoService;
    @Autowired
    private OSSService myOssService;
    @Autowired
    private StorageType storageType;


    @Override
    public Result saveCustomerServiceInfo(CustomerServiceInfoVO customerServiceInfo) {
        List<CustomerServiceInfoDTO> customerServiceInfoList = customerServiceInfo.getCustomerServiceInfoList();
        List<String> taskIds = customerServiceInfoList.stream().map(CustomerServiceInfoDTO::getTaskId).collect(Collectors.toList());
        log.info("??????????????????????????????... taskIds:{}", Arrays.asList(taskIds));
        ArrayList<CustomerServiceInfoEntity> customerServiceInfoEntities = new ArrayList<>();
        for (CustomerServiceInfoDTO customerServiceInfoDTO : customerServiceInfoList) {
            String contextInfo = saveContextInfo(customerServiceInfoDTO.getContext(), customerServiceInfoDTO.getContextType());
            CustomerServiceInfoEntity customerServiceInfoEntity = new CustomerServiceInfoEntity();
            BeanUtils.copyProperties(customerServiceInfoDTO, customerServiceInfoEntity);
            // ???????????????context?????????????????????
            customerServiceInfoEntity.setContext("");
            customerServiceInfoEntity.setInsteadContext(contextInfo);
            customerServiceInfoEntity.setInstead(1);
            customerServiceInfoEntities.add(customerServiceInfoEntity);
        }
//        returnInfoServiceDao.saveAll(customerServiceInfoEntities);
        log.info("??????????????????????????????... taskIds:{}", Arrays.asList(taskIds));
        return new Result(RespCode.SUCC, null);
    }


    public String saveContextInfo(String context, String contextType) {
        String objectName = "";
        switch (storageType) {
            case OSS_QINGSTOR_STORAGE:
            case OSS_ZANHUA_STORAGE:
            case OSS_ALI_STORAGE:
            case NAS_STORAGE:
            case H5IMG_STORAGE:

                // ??????&?????????
                if ("1".equals(contextType)) {
                    objectName = myOssService.generateResouceObjectName("OptraceCustomerResourceFile", "txt");
                    myOssService.putObject(objectName, context);
                }
                // ?????????
                if ("4".equals(contextType)) {
                    objectName = myOssService.generateResouceObjectName("OptraceCustomerResourceFile", "txt");
                    myOssService.putObject(objectName, context);
                }
                // ????????????
                if ("3".equals(contextType)) {
                    objectName = myOssService.generateResouceObjectName("OptraceCustomerResourceFile", "txt");
                    myOssService.putObject(objectName, context);
                }
                // ?????? base64
                if ("2".equals(contextType)) {
                    objectName = myOssService.generateResouceObjectName("OptraceCustomerResourceFile", "png");
                    InputStream inputStream = ImageUtils.BaseToInputStream(context);
                    myOssService.putObject(objectName, inputStream);
                }
                break;
            case MYSQL_STORAGE:
                String uuid = StUtils.getUUID16();
                String path = String.format("%s/version_%s/%s/%s", CommonContent.RECORD_INFO_UPLOAD_PATH, "OptraceCustomerResourceFile", DateUtils.dateConvertStr(), StUtils.getUUID());
                String realFileName = "";
                // ??????&?????????
                if ("1".equals(contextType) || "4".equals(contextType)) {
                    InputStream byteArrayInputStream = new ByteArrayInputStream(context.getBytes());
                    realFileName = URLUtils.str2Local(byteArrayInputStream, path, uuid + ".txt");
                }
                // ?????????
                if ("4".equals(contextType)) {
                    InputStream inputStream = ImageUtils.BaseToInputStream(context);
                    realFileName = URLUtils.str2Local(inputStream, path, uuid + ".png");
                }
                // ????????????
                if ("3".equals(contextType)) {
                    InputStream inputStream = URLUtils.url2InputStream(context);
                    realFileName = URLUtils.str2Local(inputStream, path, uuid + ".png");
                }
                // ??????
                if ("2".equals(contextType)) {
                    InputStream inputStream = ImageUtils.BaseToInputStream(context);
                    realFileName = URLUtils.str2Local(inputStream, path, uuid + ".png");

                }
                objectName = CommonContent.DO_MAIN + "/" + realFileName;
                break;
            default:
                return null;
        }
        return objectName;
    }

    @Override
    public List<CustomerServiceInfoEntity> findAllByTaskId(String taskId) {
//        return returnInfoServiceDao.findAllByTaskId(taskId);
        return null;
    }

    @Override
    public Result getCustomerServiceInfo(GetTaskDetailDTO getTaskDetailDTO) {
        List<CustomerServiceInfoEntity> all = null;
//        List<CustomerServiceInfoEntity> all = returnInfoServiceDao.findAllByTaskId(getTaskDetailDTO.getTaskId());
        // ????????????
        List<GetTaskDetailVO> collect = all.stream().map(customerServiceInfoEntity -> {
            GetTaskDetailVO getTaskDetailVO = new GetTaskDetailVO();
            BeanUtils.copyProperties(customerServiceInfoEntity, getTaskDetailVO);
            Integer instead = customerServiceInfoEntity.getInstead();
            // ????????????????????????
            if (instead != null && instead == 1) {
                // ????????????context
                String contextInfo = getContextInfo(customerServiceInfoEntity.getInsteadContext(), customerServiceInfoEntity.getContextType());
                getTaskDetailVO.setContext(contextInfo);
            }
            return getTaskDetailVO;
        }).collect(Collectors.toList());
        return new Result(RespCode.SUCC, collect);
    }

    @Override
    public String getContextInfo(String insteadContext, String contextType) {
        String objectName = "";
        switch (storageType) {
            case OSS_QINGSTOR_STORAGE:
            case OSS_ZANHUA_STORAGE:
            case OSS_ALI_STORAGE:
            case NAS_STORAGE:
            case H5IMG_STORAGE:

                // ?????? -> ????????????
                if ("1".equals(contextType)) {
                    return myOssService.getObjectContentString(insteadContext);
                }
                // ????????? -> ????????????
                if ("4".equals(contextType)) {
                    return myOssService.getObjectContentString(insteadContext);
                }
                // ???????????? -> ????????????
                if ("3".equals(contextType)) {
                    return myOssService.getObjectContentString(insteadContext);
                }
                // ?????? base64 ->????????????
                if ("2".equals(contextType)) {
                    return myOssService.getObjectSignatureUrl(insteadContext);
                }
                break;
            case MYSQL_STORAGE:
                objectName = insteadContext;
                break;
            default:
                return null;
        }
        return objectName;
    }

    @Override
    public String getContextInfoSignatureUrl(String insteadContext, String contextType) {
        String objectName = "";
        switch (storageType) {
            case OSS_QINGSTOR_STORAGE:
            case OSS_ZANHUA_STORAGE:
            case OSS_ALI_STORAGE:
            case NAS_STORAGE:
            case H5IMG_STORAGE:

                // ?????? -> ????????????
                if ("1".equals(contextType)) {
                    return myOssService.getObjectSignatureUrl(insteadContext);
                }
                // ????????? -> ????????????
                if ("4".equals(contextType)) {
                    return myOssService.getObjectSignatureUrl(insteadContext);
                }
                // ???????????? -> ????????????
                if ("3".equals(contextType)) {
                    return myOssService.getObjectSignatureUrl(insteadContext);
                }
                // ?????? base64 ->????????????
                if ("2".equals(contextType)) {
                    return myOssService.getObjectSignatureUrl(insteadContext);
                }
                break;
            case MYSQL_STORAGE:
                objectName = insteadContext;
                break;
            default:
                return null;
        }
        return objectName;
    }

    @Override
    public Result saveOptraceFile(OrderInfoDTO orderInfoDTO, MultipartFile file) {
        // ????????????????????????
        RecordInfoDTO recordInfoDTO = new RecordInfoDTO();
        recordInfoDTO.setLast(1);
        recordInfoDTO.setRecordInfo(CommonContent.DO_MAIN + "/uploads/" + file.getOriginalFilename());
        recordInfoDTO.setTaskId(new CreateTaskService().createTask());
        recordInfoDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
        recordInfoDTO.setOrderInfo(orderInfoDTO);
        recordInfoDTO.setOptraceFile(1);
        recordInfoService.recordOption(recordInfoDTO);

        switch (storageType) {
            case OSS_QINGSTOR_STORAGE:
            case OSS_ZANHUA_STORAGE:
            case OSS_ALI_STORAGE:
            case NAS_STORAGE:
            case H5IMG_STORAGE:
                try {
                    String optraceFile = myOssService.generateResouceObjectName("OptraceFile", file.getOriginalFilename());
                    myOssService.putObject(optraceFile, file.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case MYSQL_STORAGE:
                //?????????????????????
                String fileName = file.getOriginalFilename();
                //???????????????????????????????????????
                File pfile = new File(fileService.getPath());
                if (!pfile.exists()) {
                    pfile.mkdirs();
                }
                File newfile = new File(pfile, fileName);
                try {
                    file.transferTo(newfile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                return null;
        }

        return new Result(RespCode.SUCC, null);
    }
}
