package kr.ai.nemo.domain.user.service;

import kr.ai.nemo.domain.user.dto.MyPageResponse;
import kr.ai.nemo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public MyPageResponse getMyPage(Long userId) {
    return userRepository.findDtoById(userId);
  }
}
