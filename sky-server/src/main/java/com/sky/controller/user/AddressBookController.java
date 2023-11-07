package com.sky.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Slf4j
@Api(tags = "C端地址簿接口")
public class AddressBookController {

    @Autowired
    private AddressService addressBookService;

    @GetMapping("/list")
    @ApiOperation("C端获取用户地址簿")
    public Result<List<AddressBook>> list(){
        return addressBookService.listByUserId();
    }

    @PostMapping
    @ApiOperation("C端用户新增地址")
    public Result addAddressBook(@RequestBody AddressBook addressBook){

        Long userId = BaseContext.getCurrentId();

        addressBook.setUserId(userId);

        addressBookService.save(addressBook);
        return Result.success();
    }


    @GetMapping("/default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> getDefaultAddress(){

        Long userId = BaseContext.getCurrentId();

        AddressBook address = addressBookService.getOne(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, userId)
                .eq(AddressBook::getIsDefault,1));

        if (address == null){
            return Result.error("没有默认地址");
        }

        return Result.success(address);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getById(@PathVariable Long id){
        AddressBook address = addressBookService.getById(id);
        return Result.success(address);
    }

    @PutMapping
    @ApiOperation("根据id修改地址")
    public Result updateById(@RequestBody AddressBook addressBook){
        addressBookService.updateById(addressBook);
        return Result.success();
    }

    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefault(@RequestBody AddressBook addressBook){

        addressBookService.setDefault(addressBook);
        return Result.success();
    }

}
