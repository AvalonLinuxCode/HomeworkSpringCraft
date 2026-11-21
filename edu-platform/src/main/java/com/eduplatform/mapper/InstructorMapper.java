package com.eduplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduplatform.entity.Instructor;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 讲师数据访问。单条 CRUD 由 BaseMapper 提供，列表/统计见 InstructorMapper.xml。
 */
public interface InstructorMapper extends BaseMapper<Instructor> {

    List<Instructor> list(@Param("keyword") String keyword, @Param("limit") int limit, @Param("offset") int offset);

    long count(@Param("keyword") String keyword);

    /**
     * 删除讲师前的关联校验：该讲师名下课程数。
     *
     * @implNote 当前挂在 InstructorMapper 上（讲师是课程的聚合根）。
     *           随 CourseService 切片落地，此方法应迁移到 CourseMapper。
     */
    long countCoursesByInstructorId(@Param("instructorId") long instructorId);
}
