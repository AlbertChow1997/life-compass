package com.albertchow.lifecompass.support;

import com.albertchow.lifecompass.common.exception.NotFoundException;
import com.albertchow.lifecompass.entity.SupportFaq;
import com.albertchow.lifecompass.entity.SupportMessage;
import com.albertchow.lifecompass.mapper.SupportFaqMapper;
import com.albertchow.lifecompass.mapper.SupportMessageMapper;
import com.albertchow.lifecompass.support.dto.SupportAnswerResponse;
import com.albertchow.lifecompass.support.dto.SupportFaqRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Support widget: matches a user's question against admin-managed keyword
 * lists and logs every question asked (matched or not) for admin review.
 * {@code source_type}/AI hand-off is intentionally not modeled yet — this is
 * the seam where an AI responder would plug in later.
 */
@Service
@RequiredArgsConstructor
public class SupportService {

    private static final String FALLBACK_ANSWER =
            "Thanks for your question! We don't have an automatic answer for that yet — our team will follow up.";

    private final SupportFaqMapper faqMapper;
    private final SupportMessageMapper messageMapper;

    /** Tries to match the question to an FAQ entry, logs the question either way, and returns the matched answer or a fallback message. */
    public SupportAnswerResponse ask(String question, Long userId) {
        SupportFaq matched = findMatch(question);

        SupportMessage message = new SupportMessage();
        message.setUserId(userId);
        message.setQuestion(question);
        message.setMatchedFaqId(matched != null ? matched.getId() : null);
        message.setAnswerGiven(matched != null ? matched.getAnswer() : null);
        messageMapper.insert(message);

        return new SupportAnswerResponse(matched != null ? matched.getAnswer() : FALLBACK_ANSWER, matched != null);
    }

    private SupportFaq findMatch(String question) {
        String normalized = question.toLowerCase();
        List<SupportFaq> activeFaqs = faqMapper.selectList(new LambdaQueryWrapper<SupportFaq>().eq(SupportFaq::getStatus, 1));
        for (SupportFaq faq : activeFaqs) {
            for (String keyword : faq.getKeywords().split(",")) {
                String trimmed = keyword.trim().toLowerCase();
                if (!trimmed.isEmpty() && normalized.contains(trimmed)) {
                    return faq;
                }
            }
        }
        return null;
    }

    public List<SupportFaq> listFaq() {
        return faqMapper.selectList(new LambdaQueryWrapper<SupportFaq>().orderByDesc(SupportFaq::getCreateTime));
    }

    public SupportFaq createFaq(SupportFaqRequest request) {
        SupportFaq faq = new SupportFaq();
        faq.setKeywords(request.keywords());
        faq.setAnswer(request.answer());
        faq.setStatus(1);
        faqMapper.insert(faq);
        return faq;
    }

    public SupportFaq updateFaq(Long id, SupportFaqRequest request) {
        SupportFaq faq = faqMapper.selectById(id);
        if (faq == null) {
            throw new NotFoundException("FAQ entry not found");
        }
        faq.setKeywords(request.keywords());
        faq.setAnswer(request.answer());
        faqMapper.updateById(faq);
        return faq;
    }

    public void deleteFaq(Long id) {
        if (faqMapper.selectById(id) == null) {
            throw new NotFoundException("FAQ entry not found");
        }
        faqMapper.deleteById(id);
    }

    public List<SupportMessage> listMessages() {
        return messageMapper.selectList(new LambdaQueryWrapper<SupportMessage>().orderByDesc(SupportMessage::getCreateTime));
    }
}
