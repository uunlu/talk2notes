package com.ugur.talk2notes.repository;

import com.ugur.talk2notes.model.AudioFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AudioFileRepository extends JpaRepository<AudioFile, Long> {
  List<AudioFile> findByUserIdOrderByUploadedAtDesc(Long userId);
}
