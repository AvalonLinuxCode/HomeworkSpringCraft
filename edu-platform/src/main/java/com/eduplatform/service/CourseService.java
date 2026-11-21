package com.eduplatform.service;

import com.eduplatform.common.PageResult;
import com.eduplatform.dto.CourseDTO;
import com.eduplatform.dto.CourseQuery;
import com.eduplatform.vo.CourseDetailVO;
import com.eduplatform.vo.CourseVO;

import java.util.List;

/**
 * 课程服务：CRUD、上下架、搜索。
 */
public interface CourseService {

    /** 课程列表（分页 + 多条件筛选） */
    PageResult<CourseVO> list(CourseQuery query);

    /** 课程详情 */
    CourseDetailVO getById(Long id);

    /** 创建课程 */
    Long create(CourseDTO dto);

    /** 更新课程 */
    void update(Long id, CourseDTO dto);

    /** 删除课程 */
    void delete(Long id);

    /** 上架课程 */
    void publish(Long id);

    /** 下架课程 */
    void unpublish(Long id);

    /** 前台搜索：仅返回已上架课程 */
    PageResult<CourseVO> search(String keyword, int pageNum, int pageSize);

    /** 按讲师查询课程（status 为 null 时返回全部状态） */
    List<CourseVO> listByInstructor(Long instructorId, String status);
}
