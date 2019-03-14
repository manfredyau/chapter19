package com.ssm.chapter18.service.impl;

import com.ssm.chapter18.dao.RoleDao;
import com.ssm.chapter18.pojo.Role;
import com.ssm.chapter18.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleDao roleDao;

    /**
     * 使用@Cacheable定義緩存策略
     * 當緩存中有值，则返回緩存數據，否则調用方法得到數據
     * 通過 value 引用緩存管理器，通過 key 定義鍵
     *
     * @param id 角色編號
     * @return 角色
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    @Cacheable(value = "redisCacheManager", key = "'redis_role_'+#id")
    public Role getRole(Long id) {
        return roleDao.getRole(id);
    }

    @Override
    public int deleteRole(Long id) {
        return 0;
    }

    /**
     * 使用 @{@link org.springframework.cache.annotation.CachePut} 則表示無論如何都會執行方法，最後將方法的返回值再保存到緩存中
     * 使用在插入數據的地方，則表示數據保存到數據庫後，會同期插入 redis 緩存中
     *
     * @param role 角色對象
     * @return 角色對象(會回填主鍵)
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    @CachePut(value = "redisCacheManager", key = "'redis_role_'+#role.id")
    public Role insertRole(Role role) {
        roleDao.insertRole(role);
        return role;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    @CachePut(value = "redisCacheManager", key = "'redis_role_'+#role.id")
    public Role updateRole(Role role) {
        roleDao.updateRole(role);
        return role;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public List<Role> findRoles(String roleName, String note) {
        return roleDao.findRoles(roleName, note);
    }
}
