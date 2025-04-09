package org.nsu.learn.orm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.junit.BeforeClass;
import org.nsu.learn.orm.beans.Course;
import org.nsu.learn.orm.beans.Message;
import org.nsu.learn.orm.beans.NsuGroup;
import org.nsu.learn.orm.beans.Report;
import org.nsu.learn.orm.beans.StudyMaterial;
import org.nsu.learn.orm.beans.user.UserAccount;
import org.nsu.learn.orm.beans.user.UserRole;
import org.nsu.learn.orm.services.CourseService;
import org.nsu.learn.orm.services.MessageService;
import org.nsu.learn.orm.services.NsuGroupService;
import org.nsu.learn.orm.services.ReportService;
import org.nsu.learn.orm.services.StudyMaterialService;
import org.nsu.learn.orm.services.UserService;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    private UserService userService;

    private NsuGroupService groupService;

    private ReportService reportService;

    private MessageService messageService;

    private StudyMaterialService studyMaterialService;

    private CourseService courseService;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static TestSuite suite() {
        return new TestSuite(AppTest.class);
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        File f = new File("nsubase");
        deleteDirectory(f);
    }

    public void testCreation() {
        UserAccount user = new UserAccount("makarov", "makarov", "Николай", "Макаров", "Алексеевич");
        user = userService.saveUser(user);
        assertNotNull(user.getId());
        UserAccount user2 = new UserAccount("ivanov", "ivanov", "Иван", "Иванов", "Иванович");
        user2 = userService.saveUser(user2);
        assertNotNull(user2.getId());
        UserAccount teacher = new UserAccount("vas", "vas", "Татьяна", "Васючкова", "Cергеевна");
        teacher = userService.saveUser(teacher);
        assertNotNull(teacher.getId());
        List<Criterion> criterions = new ArrayList<Criterion>();
        criterions.add(Restrictions.eq(UserAccount.FIEILD_LOGIN, "makarov"));
        criterions.add(Restrictions.eq(UserAccount.FIELD_FIRSTNAME, "Николай"));
        List<UserAccount> usersList = userService.getUsersListByCriterion(criterions);
        assertEquals(usersList.size(), 1);
        criterions = new ArrayList<Criterion>();
        criterions.add(Restrictions.eq(UserAccount.FIEILD_LOGIN, "ivanov"));
        criterions.add(Restrictions.eq(UserAccount.FIELD_FIRSTNAME, "Николай"));
        usersList = userService.getUsersListByCriterion(criterions);
        assertEquals(usersList.size(), 0);
        userService.addUserRoleToUserAccount(user.getId(), userService.createUserRole(UserRole.GUEST).getId());
        userService.addUserRoleToUserAccount(user.getId(), userService.createUserRole(UserRole.STUDENT).getId());
        userService.addUserRoleToUserAccount(teacher.getId(), userService.createUserRole(UserRole.TEACHER).getId());
        NsuGroup group = new NsuGroup("8208", "Базовая группа для студентов");
        group = groupService.saveGroup(group);
        assertNotNull(group.getId());
        groupService.addStudentToNsuGroup(user.getId(), group.getId());
        Set<NsuGroup> groupsSet = new HashSet<NsuGroup>();
        groupsSet.add(group);
        Report report = new Report("Все пользователи", "Выдает всех пользователей", "brrr");
        reportService.saveReport(report);
        Message message = new Message(user.getId(), user2.getId(), "Тестовое сообщение", "hjkhjk", true);
        assertNotNull(messageService.save(message));
        Message message2 = new Message(user.getId(), user2.getId(), "Тестовое сообщение 2", "kl;kl;kl;", true);
        assertNotNull(messageService.save(message2));
        StudyMaterial studyMaterial = new StudyMaterial("История в картинках", "самые красивые истории", "www");
        studyMaterialService.saveStudyMaterial(studyMaterial);
        Set<StudyMaterial> materials = new HashSet<StudyMaterial>();
        materials.add(studyMaterial);
        Course course = new Course("Курс по Истории", "Курс предназначен для подготовки студентов ФИТ к сдаче Истории");
        course.setMaterials(materials);
        course.setGroups(groupsSet);
        course.setTeacher(teacher);
        course = courseService.saveCourse(course);
        assertNotNull(course.getId());
    }

    public void testSelect() {
        List<UserAccount> usersList = userService.getUsersList();
    }

    public void testApp() {
        assertTrue(true);
    }

    protected void setUp() throws Exception {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        assertNotNull(ctx);
        userService = (UserService) ctx.getBean("userService");
        assertNotNull(userService);
        groupService = (NsuGroupService) ctx.getBean("nsuGroupService");
        reportService = (ReportService) ctx.getBean("reportService");
        messageService = (MessageService) ctx.getBean("messageService");
        studyMaterialService = (StudyMaterialService) ctx.getBean("studyMaterialService");
        courseService = (CourseService) ctx.getBean("courseService");
    }

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
}
