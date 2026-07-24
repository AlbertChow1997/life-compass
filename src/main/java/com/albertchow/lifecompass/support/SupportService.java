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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Support widget: matches a user's question against admin-managed keyword
 * lists first (fast, free, deterministic for questions an admin has already
 * curated), falls back to a real AI answer via {@link DeepSeekClient} when
 * nothing matches and a key is configured, and logs every question asked
 * (matched, AI-answered, or neither) for admin review.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupportService {

    private static final String FALLBACK_ANSWER =
            "Thanks for your question! We don't have an automatic answer for that yet — our team will follow up.";

    private final SupportFaqMapper faqMapper;
    private final SupportMessageMapper messageMapper;
    private final DeepSeekClient deepSeekClient;

    /** Tries an FAQ match, then a real AI answer, then a generic fallback; logs the question either way. */
    public SupportAnswerResponse ask(String question, Long userId) {
        SupportFaq matched = findMatch(question);

        String answer;
        String source;
        if (matched != null) {
            answer = matched.getAnswer();
            source = "faq";
        } else if (deepSeekClient.isConfigured()) {
            String aiAnswer = tryAi(question);
            if (aiAnswer != null) {
                answer = aiAnswer;
                source = "ai";
            } else {
                answer = FALLBACK_ANSWER;
                source = "fallback";
            }
        } else {
            answer = FALLBACK_ANSWER;
            source = "fallback";
        }

        SupportMessage message = new SupportMessage();
        message.setUserId(userId);
        message.setQuestion(question);
        message.setMatchedFaqId(matched != null ? matched.getId() : null);
        message.setAnswerGiven(answer);
        messageMapper.insert(message);

        return new SupportAnswerResponse(answer, matched != null, source);
    }

    /** Calls DeepSeek and returns its answer, or null if the call fails — callers fall back to the generic message. */
    private String tryAi(String question) {
        try {
            return deepSeekClient.ask(question);
        } catch (RuntimeException e) {
            log.warn("AI support answer failed, falling back to the generic message", e);
            return null;
        }
    }

    /** Finds the first active FAQ entry whose comma-separated keyword list contains a case-insensitive substring of the question. */
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

    /** Lists every FAQ entry (including inactive ones), newest first. */
    public List<SupportFaq> listFaq() {
        return faqMapper.selectList(new LambdaQueryWrapper<SupportFaq>().orderByDesc(SupportFaq::getCreateTime));
    }

    /** Creates a new active FAQ entry. */
    public SupportFaq createFaq(SupportFaqRequest request) {
        SupportFaq faq = new SupportFaq();
        faq.setKeywords(request.keywords());
        faq.setAnswer(request.answer());
        faq.setStatus(1);
        faqMapper.insert(faq);
        return faq;
    }

    /** Updates an existing FAQ entry's keywords and answer. */
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

    /** Permanently removes an FAQ entry. */
    public void deleteFaq(Long id) {
        if (faqMapper.selectById(id) == null) {
            throw new NotFoundException("FAQ entry not found");
        }
        faqMapper.deleteById(id);
    }

    /** Lists every question ever submitted to the support widget, newest first, for admin review. */
    public List<SupportMessage> listMessages() {
        return messageMapper.selectList(new LambdaQueryWrapper<SupportMessage>().orderByDesc(SupportMessage::getCreateTime));
    }
}
