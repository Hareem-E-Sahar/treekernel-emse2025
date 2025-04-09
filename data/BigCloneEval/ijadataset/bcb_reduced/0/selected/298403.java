package com.germinus.xpression.liferay.groupware;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.germinus.xpression.groupware.AuthorizatorException;
import com.germinus.xpression.groupware.GroupwareRole;
import com.germinus.xpression.i18n.I18NUtils;
import com.germinus.xpression.liferay.LiferayRuntimeException;
import com.liferay.portal.PortalException;
import com.liferay.portal.SystemException;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.security.permission.ResourceActionsUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;

public class CompanyRoles {

    static final List<String> SYSTEM_ROLE_NAMES = loadSystemRoles();

    private long companyId;

    private GroupwareRole guestRole;

    private GroupwareRole userRole;

    private GroupwareRole communityMemberRole;

    private GroupwareRole organizationMemberRole;

    private List<GroupwareRole> sortedRoles;

    private static final String REQUIRED_PREFIX_PATTERN = "Grupo ";

    public CompanyRoles(long companyId) throws AuthorizatorException {
        super();
        this.companyId = companyId;
        initRoles();
    }

    List<GroupwareRole> allRoles(Group group) throws SystemException {
        List<GroupwareRole> result = systemRolesFor(group);
        result.addAll(customGroupRoles(group));
        return result;
    }

    public List<GroupwareRole> systemRolesFor(Group group) {
        ArrayList<GroupwareRole> systemRoles = new ArrayList<GroupwareRole>(sortedRoles);
        if (group.isCommunity()) systemRoles.add(communityMemberRole); else if (group.isOrganization()) systemRoles.add(organizationMemberRole);
        return systemRoles;
    }

    public List<GroupwareRole> customGroupRoles(Group group) throws SystemException {
        int communityType = RoleConstants.TYPE_COMMUNITY;
        if (group.isOrganization()) communityType = RoleConstants.TYPE_ORGANIZATION;
        List<Role> customGroupRoles = ResourceActionsUtil.getRoles(companyId, group, LiferayAuthorizator.directorFolderClass.getName());
        List<GroupwareRole> result = new ArrayList<GroupwareRole>();
        for (Role candidateRole : customGroupRoles) {
            String roleName = candidateRole.getName();
            boolean isASystemRole = false;
            if (!SYSTEM_ROLE_NAMES.contains(roleName)) {
                if ((candidateRole.getType() == communityType) && matchRequiredPattern(roleName)) {
                    result.add(new GroupwareRole(candidateRole.getRoleId(), candidateRole.getTitle(I18NUtils.getThreadLocale()), isASystemRole));
                }
            }
        }
        return result;
    }

    private boolean matchRequiredPattern(String roleName) {
        return roleName.startsWith(REQUIRED_PREFIX_PATTERN) && roleName.endsWith(")");
    }

    private void initRoles() throws AuthorizatorException {
        Role liferayUserRole;
        Role liferayGuestRole;
        Role liferayCommunityMemberRole;
        Role liferayOrganizationMemberRole;
        try {
            liferayUserRole = RoleLocalServiceUtil.getRole(companyId, RoleConstants.USER);
            liferayGuestRole = RoleLocalServiceUtil.getRole(companyId, RoleConstants.GUEST);
            liferayCommunityMemberRole = RoleLocalServiceUtil.getRole(companyId, RoleConstants.COMMUNITY_MEMBER);
            liferayOrganizationMemberRole = RoleLocalServiceUtil.getRole(companyId, RoleConstants.ORGANIZATION_MEMBER);
        } catch (PortalException e) {
            throw new AuthorizatorException(e);
        } catch (SystemException e) {
            throw new LiferayRuntimeException(e);
        }
        guestRole = new GroupwareRole(liferayGuestRole.getRoleId(), liferayGuestRole.getName(), true);
        userRole = new GroupwareRole(liferayUserRole.getRoleId(), liferayUserRole.getName(), true);
        communityMemberRole = new GroupwareRole(liferayCommunityMemberRole.getRoleId(), liferayCommunityMemberRole.getName().replaceAll(" ", ""), true);
        organizationMemberRole = new GroupwareRole(liferayOrganizationMemberRole.getRoleId(), liferayOrganizationMemberRole.getName().replaceAll(" ", ""), true);
        sortedRoles = new ArrayList<GroupwareRole>();
        sortedRoles.add(guestRole);
        sortedRoles.add(userRole);
    }

    private static List<String> loadSystemRoles() {
        String[] result = null;
        if (SYSTEM_ROLE_NAMES == null) {
            result = new String[0];
            result = appendArrays(result, RoleConstants.SYSTEM_ROLES);
            result = appendArrays(result, RoleConstants.SYSTEM_COMMUNITY_ROLES);
            result = appendArrays(result, RoleConstants.SYSTEM_ORGANIZATION_ROLES);
        }
        return Arrays.asList(result);
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] appendArrays(T[] array1, T[] array2) {
        Class<?> array1Class = array1.getClass();
        T[] newArray = (T[]) Array.newInstance(array1Class.getComponentType(), array1.length + array2.length);
        System.arraycopy(array1, 0, newArray, 0, array1.length);
        System.arraycopy(array2, 0, newArray, array1.length, array2.length);
        return newArray;
    }
}
