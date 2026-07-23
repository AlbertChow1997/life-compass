package com.albertchow.lifecompass.shop;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.ShopType;
import com.albertchow.lifecompass.mapper.ShopTypeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public endpoint for the list of shop/business categories (e.g. "Cafe",
 * "Restaurant"), used by the frontend to build its filter facets.
 */
@RestController
@RequestMapping("/api/shop-type")
@RequiredArgsConstructor
public class ShopTypeController {

    private final ShopTypeMapper shopTypeMapper;

    /** Returns all shop types in their configured display order. */
    @GetMapping
    public Result<List<ShopType>> list() {
        var query = new LambdaQueryWrapper<ShopType>().orderByAsc(ShopType::getSort);
        return Result.ok(shopTypeMapper.selectList(query));
    }
}
