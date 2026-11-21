package com.eduplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduplatform.entity.Course;
import com.eduplatform.vo.CourseDetailVO;
import com.eduplatform.vo.CourseVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程数据访问。insert/update 走自定义 XML（text[] / jsonb 需要显式处理）。
 */
public interface CourseMapper extends BaseMapper<Course> {

    List<CourseVO> list(@Param("keyword") String keyword, @Param("instructorId") Long instructorId,
                        @Param("categoryId") Long categoryId, @Param("status") String status,
                        @Param("limit") int limit, @Param("offset") int offset);

    long count(@Param("keyword") String keyword, @Param("instructorId") Long instructorId,
               @Param("categoryId") Long categoryId, @Param("status") String status);

    CourseDetailVO selectDetailById(@Param("id") Long id);

    List<CourseVO> listByInstructor(@Param("instructorId") Long instructorId, @Param("status") String status);

    int insertCourse(Course course);

    int updateCourse(Course course);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    long countByInstructorId(@Param("instructorId") Long instructorId);
}
