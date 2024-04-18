package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;
    /**
     * 新增地址
     * @param addressBook
     */
    @Override
    public void save(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressBookMapper.save(addressBook);
    }


    /**
     * 查询当前登录用户的所有地址信息
     * @param addressBook
     * @return
     */
    @Override
    public List<AddressBook> list(AddressBook addressBook) {
        List<AddressBook> addressBookList = addressBookMapper.list(addressBook);
        return addressBookList;
    }

    /**
     * 设置默认地址
     * @param addressBook
     */
    @Override
    @Transactional
    public void setDefault(AddressBook addressBook) {
        // 将当前用户地址设置成非默认
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressBookMapper.updateDefaultByUserId(addressBook);
        // 设置默认地址
        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);


    }

    /**
     * 根据id获取地址
     * @param id
     * @return
     */
    @Override
    public AddressBook getById(Long id) {
        AddressBook addressBook = addressBookMapper.getById(id);
        return addressBook;
    }

    /**
     * 修改地址
     * @param addressBook
     */
    @Override
    public void update(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.update(addressBook);
    }

    /**
     * 根据id删除地址
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }


}
