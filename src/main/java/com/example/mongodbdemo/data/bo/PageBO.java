package com.example.mongodbdemo.data.bo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/7/23 18:07
 **/
@Data
public class PageBO<T> {

    /**
     * 当前页
     */
    private Integer currPage;
    /**
     * 总页数
     */
    private Integer totalPage;
    /**
     * 总记录数
     */
    private Integer totalCount;
    /**
     * 当前页数据
     */
    private T data;

    /**
     * @param data       数据
     * @param currPage   当前第几页
     * @param totalCount 总条数
     * @param pageNumber 每页展示条数
     * @param <T>
     * @return
     */
    public static <T> PageBO<T> handler(T data, Integer currPage, Integer totalCount, Integer pageNumber) {

        PageBO<T> pageBO = new PageBO<T>();
        pageBO.setData(data);
        pageBO.setCurrPage(currPage);
        pageBO.setTotalCount(totalCount);

        // 处理页数
        int totalPage = totalCount / pageNumber;
        if (totalCount % pageNumber != 0) {
            totalPage++;
        }
        pageBO.setTotalPage(totalPage);

        return pageBO;
    }

    /**
     * @param data       数据
     * @param currPage   当前第几页
     * @param totalCount 总条数
     * @param totalPage  总页数
     * @param <T>
     * @return
     */
    public static <T> PageBO<T> directHandler(T data, Integer currPage, Integer totalCount, Integer totalPage) {

        PageBO<T> pageBO = new PageBO<T>();
        pageBO.setData(data);
        pageBO.setCurrPage(currPage);
        pageBO.setTotalCount(totalCount);
        pageBO.setTotalPage(totalPage);
        return pageBO;
    }


    /**
     * @param currPage   当前第几页
     * @param totalCount 总条数
     * @param totalPage  总页数
     * @param <T>
     * @return
     */
    public static PageBO<List<Object>> emptyHandler(Integer currPage, Integer totalCount, Integer totalPage) {

        PageBO<List<Object>> pageBO = new PageBO<List<Object>>();
        pageBO.setData(new ArrayList<Object>());
        pageBO.setCurrPage(currPage);
        pageBO.setTotalCount(totalCount);
        pageBO.setTotalPage(totalPage);
        return pageBO;
    }
}
