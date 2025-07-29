package com.github.stepwise.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.stepwise.repository.AcademicWorkRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AcademicWorkService {

  private final AcademicWorkRepository academicWorkRepository;


}
