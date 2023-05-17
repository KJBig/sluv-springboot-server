package com.sluv.server.domain.question.entity;

import com.sluv.server.domain.question.enums.QuestionStatus;
import com.sluv.server.domain.user.entity.User;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
//@SuperBuilder
@DiscriminatorValue("Buy")
public class QuestionBuy extends Question{


    private LocalDateTime voteEndTime;

    @Builder
    public QuestionBuy(Long id, User user, String title, String content, Long searchNum, QuestionStatus questionStatus, LocalDateTime voteEndTime) {
        super(id, user, title, content, searchNum, questionStatus);
        this.voteEndTime = voteEndTime;
    }
}
