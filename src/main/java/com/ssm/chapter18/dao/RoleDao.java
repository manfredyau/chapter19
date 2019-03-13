package com.ssm.chapter18.dao;

import com.ssm.chapter18.pojo.Role;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleDao {
    Role getRole(Long id);

    int deleteRole(Long id);

    int insertRole(Role role);

    int updateRole(Role role);

    List<Role> findRoles(@Param("roleName") String roleName, @Param("note") String note);
}
