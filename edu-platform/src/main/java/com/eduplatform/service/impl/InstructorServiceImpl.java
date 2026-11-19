package com.eduplatform.service.impl;

import com.eduplatform.common.BusinessException;
import com.eduplatform.common.NotFoundException;
import com.eduplatform.common.PageResult;
import com.eduplatform.common.ValidationException;
import com.eduplatform.dto.InstructorDTO;
import com.eduplatform.dto.InstructorQuery;
import com.eduplatform.entity.Instructor;
import com.eduplatform.mapper.InstructorMapper;
import com.eduplatform.service.InstructorService;
import com.eduplatform.vo.InstructorVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InstructorServiceImpl implements InstructorService {

    private final InstructorMapper instructorMapper;

    public InstructorServiceImpl(InstructorMapper instructorMapper) {
        this.instructorMapper = instructorMapper;
    }

    @Override
    @Transactional
    public Long create(InstructorDTO dto) {
        Instructor entity = new Instructor();
        apply(dto, entity);
        instructorMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public InstructorVO getById(Long id) {
        return InstructorVO.from(requireInstructor(id));
    }

    @Override
    @Transactional
    public void update(Long id, InstructorDTO dto) {
        Instructor entity = requireInstructor(id);
        apply(dto, entity);
        instructorMapper.updateById(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        long courses = instructorMapper.countCoursesByInstructorId(id);
        if (courses > 0) {
            throw new BusinessException("该讲师名下存在 " + courses + " 门课程，无法删除");
        }
        instructorMapper.deleteById(id);
    }

    @Override
    public PageResult<InstructorVO> list(InstructorQuery query) {
        long total = instructorMapper.count(query.keyword());
        List<InstructorVO> records = instructorMapper
                .list(query.keyword(), query.pageSize(), (query.pageNum() - 1) * query.pageSize())
                .stream()
                .map(InstructorVO::from)
                .toList();
        return new PageResult<>(total, records);
    }

    private static void apply(InstructorDTO dto, Instructor entity) {
        if (dto.name() == null || dto.name().isBlank()) {
            throw new ValidationException("讲师姓名不能为空");
        }
        entity.setName(dto.name().trim());
        entity.setAvatar(dto.avatar());
        entity.setTitle(dto.title());
        entity.setDescription(dto.description());
    }

    private Instructor requireInstructor(Long id) {
        Instructor entity = id == null ? null : instructorMapper.selectById(id);
        if (entity == null) {
            throw new NotFoundException("讲师不存在: id=" + id);
        }
        return entity;
    }
}
