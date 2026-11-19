package com.eduplatform.service;

import com.eduplatform.common.PageResult;
import com.eduplatform.dto.InstructorDTO;
import com.eduplatform.dto.InstructorQuery;
import com.eduplatform.vo.InstructorVO;

public interface InstructorService {

    Long create(InstructorDTO dto);

    InstructorVO getById(Long id);

    void update(Long id, InstructorDTO dto);

    void delete(Long id);

    PageResult<InstructorVO> list(InstructorQuery query);
}
