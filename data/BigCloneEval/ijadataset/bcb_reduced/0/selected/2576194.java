package org.njo.webapp.root.model;

import java.io.Serializable;
import java.lang.reflect.Array;

/**
 * 这个类用于保存用户信息.
 * 包括以下属性.
 * 1,用户名
 * 2,密码
 * 3,用户所拥有的角色列表
 *
 * 注意:
 * 1,这个类中本来不应该有password属性,
 *   但是为了能够和subversion的模块整合才加入这个属性.
 *   访问subversion时需要使用用户名和密码.
 *
 * @author yu.peng
 * @version 0.01
 */
public class RemoteUser implements Serializable {

    private String name;

    private String password;

    private String[] roles = new String[0];

    public RemoteUser() {
    }

    public RemoteUser(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        if (roles == null) {
            this.roles = new String[0];
            return;
        }
        this.roles = roles;
    }

    /**
     * 向用户拥有的角色列表中加入角色.
     *
     * @param role
     */
    public void addRole(String role) {
        int arrayLength = Array.getLength(this.roles);
        String[] newArray = (String[]) Array.newInstance(this.roles.getClass().getComponentType(), arrayLength + 1);
        System.arraycopy(this.roles, 0, newArray, 0, arrayLength);
        newArray[arrayLength] = role;
        this.roles = newArray;
    }

    /**
     * 判断这个用户是否拥有某一组指定角色.
     *
     * @param roles
     * @return boolean
     */
    public boolean isInRole(String roles) {
        if (this.roles == null) {
            return false;
        }
        if (roles == null) {
            return false;
        }
        String[] reqRoles = roles.split(",");
        for (int i0 = 0; i0 < reqRoles.length; i0++) {
            String reqRole = reqRoles[i0];
            for (int i = 0; i < this.roles.length; i++) {
                if (reqRole.equals(this.roles[i])) {
                    return true;
                }
            }
        }
        return false;
    }
}
