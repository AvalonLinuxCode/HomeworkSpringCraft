package com.eduplatform.service;

import com.eduplatform.common.PageResult;
import com.eduplatform.dto.InstructorDTO;
import com.eduplatform.dto.InstructorQuery;
import com.eduplatform.vo.InstructorVO;

import java.util.List;

/**
 * 讲师服务：CRUD 与列表查询。
 */
public interface InstructorService {

    Long create(InstructorDTO dto);

    void delete(Long id);

    InstructorVO getById(Long id);

    PageResult<InstructorVO> list(InstructorQuery query);

    List<InstructorVO> listAll();

    void update(Long id, InstructorDTO dto);
}
