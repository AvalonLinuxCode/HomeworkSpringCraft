package com.eduplatform.service.impl;

import com.eduplatform.common.BusinessException;
import com.eduplatform.common.NotFoundException;
import com.eduplatform.common.PageResult;
import com.eduplatform.common.ValidationException;
import com.eduplatform.dto.CourseDTO;
import com.eduplatform.dto.CourseQuery;
import com.eduplatform.entity.Course;
import com.eduplatform.mapper.CategoryMapper;
import com.eduplatform.mapper.CourseMapper;
import com.eduplatform.mapper.InstructorMapper;
import com.eduplatform.service.CourseService;
import com.eduplatform.vo.CourseDetailVO;
import com.eduplatform.vo.CourseVO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final InstructorMapper instructorMapper;
    private final CategoryMapper categoryMapper;

    public CourseServiceImpl(CourseMapper courseMapper, InstructorMapper instructorMapper,
                             CategoryMapper categoryMapper) {
        this.courseMapper = courseMapper;
        this.instructorMapper = instructorMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CourseVO> list(CourseQuery query) {
        long total = courseMapper.count(query.keyword(), query.instructorId(), query.categoryId(), query.status());
        if (total == 0) {
            return new PageResult<>(0, List.of());
        }
        int offset = (query.pageNum() - 1) * query.pageSize();
        List<CourseVO> records = courseMapper.list(query.keyword(), query.instructorId(),
                query.categoryId(), query.status(), query.pageSize(), offset);
        return new PageResult<>(total, records);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailVO getById(Long id) {
        CourseDetailVO detail = id == null ? null : courseMapper.selectDetailById(id);
        if (detail == null) {
            throw new NotFoundException("课程不存在");
        }
        return detail;
    }

    @Override
    @Transactional
    public Long create(CourseDTO dto) {
        Course course = new Course();
        apply(course, dto);
        courseMapper.insertCourse(course);
        return course.getId();
    }

    @Override
    @Transactional
    public void update(Long id, CourseDTO dto) {
        Course course = requireCourse(id);
        apply(course, dto);
        courseMapper.updateCourse(course);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireCourse(id);
        try {
            courseMapper.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("课程存在订单记录，无法删除");
        }
    }

    @Override
    @Transactional
    public void publish(Long id) {
        Course course = requireCourse(id);
        if ("published".equals(course.getStatus())) {
            throw new BusinessException("课程已上架，无需重复上架");
        }
        courseMapper.updateStatus(id, "published");
    }

    @Override
    @Transactional
    public void unpublish(Long id) {
        Course course = requireCourse(id);
        if (!"published".equals(course.getStatus())) {
            throw new BusinessException("课程未上架，无法下架");
        }
        courseMapper.updateStatus(id, "draft");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CourseVO> search(String keyword, int pageNum, int pageSize) {
        return list(new CourseQuery(keyword, pageNum, pageSize, null, null, "published"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseVO> listByInstructor(Long instructorId, String status) {
        if (instructorId == null) {
            throw new ValidationException("讲师 ID 不能为空");
        }
        return courseMapper.listByInstructor(instructorId, status);
    }

    private Course requireCourse(Long id) {
        Course course = id == null ? null : courseMapper.selectById(id);
        if (course == null) {
            throw new NotFoundException("课程不存在");
        }
        return course;
    }

    /**
     * 校验并填充课程字段。create 与 update 共用同一套规则。
     */
    private void apply(Course course, CourseDTO dto) {
        if (dto == null) {
            throw new ValidationException("课程信息不能为空");
        }
        String title = dto.title() == null ? null : dto.title().trim();
        if (title == null || title.isEmpty()) {
            throw new ValidationException("课程标题不能为空");
        }
        if (title.length() > 200) {
            throw new ValidationException("课程标题不能超过 200 个字符");
        }
        if (dto.instructorId() == null) {
            throw new ValidationException("课程必须指定讲师");
        }
        if (instructorMapper.selectById(dto.instructorId()) == null) {
            throw new NotFoundException("讲师不存在");
        }
        if (dto.categoryId() != null && categoryMapper.selectById(dto.categoryId()) == null) {
            throw new NotFoundException("分类不存在");
        }
        BigDecimal price = dto.price() == null ? BigDecimal.ZERO : dto.price();
        if (price.signum() < 0) {
            throw new ValidationException("课程价格不能为负数");
        }
        if (price.scale() > 2) {
            throw new ValidationException("课程价格最多两位小数");
        }
        course.setTitle(title);
        course.setDescription(dto.description());
        course.setPrice(price);
        course.setInstructorId(dto.instructorId());
        course.setCategoryId(dto.categoryId());
        course.setCoverImage(dto.coverImage());
        course.setTags(normalizeTags(dto.tags()));
        course.setDetails(normalizeDetails(dto.details()));
    }

    private static String[] normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        Set<String> distinct = new LinkedHashSet<>();
        for (String tag : tags) {
            if (tag != null && !tag.isBlank()) {
                distinct.add(tag.trim());
            }
        }
        return distinct.isEmpty() ? null : distinct.toArray(new String[0]);
    }

    private static String normalizeDetails(String details) {
        return (details == null || details.isBlank()) ? null : details;
    }
}
