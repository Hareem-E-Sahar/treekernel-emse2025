package br.com.visualmidia.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;
import org.joda.time.DateTimeZone;
import br.com.visualmidia.GD;
import br.com.visualmidia.business.Account;
import br.com.visualmidia.business.ClassRoom;
import br.com.visualmidia.business.Course;
import br.com.visualmidia.business.Person;
import br.com.visualmidia.core.Constants;
import br.com.visualmidia.persistence.GetAccounts;
import br.com.visualmidia.persistence.GetClassRoom;
import br.com.visualmidia.persistence.GetCourse;
import br.com.visualmidia.persistence.GetEmployee;
import br.com.visualmidia.persistence.GetProperty;
import br.com.visualmidia.persistence.SetProperty;
import br.com.visualmidia.system.Anakin;
import br.com.visualmidia.system.GDSystem;
import br.com.visualmidia.system.GDWindowControl;
import br.com.visualmidia.system.PidManager;
import br.com.visualmidia.tools.StringTool;
import br.com.visualmidia.ui.controlcenter.ControlCenter;
import br.com.visualmidia.ui.dialog.LoadStudentDialog;
import br.com.visualmidia.ui.preferences.ShowFieldPrefs;
import br.com.visualmidia.ui.splash.SplashScreen;
import br.com.visualmidia.update.UpdateSetup;

/**
 * @author   Lucas
 */
public class MainScreen {

    private SashForm mainPanel;

    private Menu menu;

    private GDSystem system;

    private Composite statusPanel;

    private CTabFolder cTabFolder;

    private Image image;

    private Shell shell;

    private Display display;

    private CTabFolder inicialConfigurationcTabFolder;

    private GDWindowControl windowControl;

    private Label statusMessage;

    private Label statusHour;

    @SuppressWarnings("unchecked")
    public void run() {
        GDSystem system = GDSystem.getInstance();
        try {
            Map<String, Object> properties = (Map<String, Object>) system.query(new GetProperty());
            if (properties.get("timezone") == null) {
                system.execute(new SetProperty("timezone", "America/Sao_Paulo"));
            }
            DateTimeZone zone = DateTimeZone.forID(String.valueOf(properties.get("timezone")));
            DateTimeZone.setDefault(zone);
            display = (Display.getCurrent() != null) ? Display.getCurrent() : new Display();
            shell = new Shell(display);
            loadSystem(shell);
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
            if (GDSystem.isClientMode()) {
                runCloseProcess(GDSystem.isClientMode());
            } else {
                runCloseProcess(false);
            }
            shell.dispose();
            display.dispose();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void runCloseProcess(final boolean isClientMode) {
        IRunnableWithProgress progress = new IRunnableWithProgress() {

            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask("Fechando o Grente Digital. Aguarde...", IProgressMonitor.UNKNOWN);
                monitor.subTask("Executando snapshot.");
                File files = new File(Constants.PREVAYLER_DATA_DIRETORY);
                for (File file : files.listFiles()) {
                    if (isClientMode || file.getName().contains(".snapshot")) file.delete();
                }
                system.takeSnapshot();
                monitor.subTask("Apagando arquivos tempor�rios...");
                deleteDir(Constants.TEMP_DIR);
                monitor.done();
            }
        };
        try {
            ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(shell);
            monitorDialog.run(true, false, progress);
        } catch (InvocationTargetException e) {
            MessageDialog.openError(shell, "Error", e.getMessage());
        } catch (InterruptedException e) {
            MessageDialog.openInformation(shell, "Cancelled", e.getMessage());
        }
    }

    private void loadSystem(final Shell shell) {
        final int waitTime = 375;
        final SplashScreen splashScreen = new SplashScreen(getShell());
        splashScreen.setRaiseAmount(waitTime);
        splashScreen.setWaitTime(500);
        splashScreen.open();
        display.asyncExec(new Runnable() {

            public void run() {
                try {
                    splashScreen.setMessage("Restaurando base de dados");
                    splashScreen.raiseBar();
                    system = GDSystem.getInstance();
                    splashScreen.setMessage("Inicializando registros");
                    splashScreen.raiseBar();
                    GDSystem.getGDRegistry();
                    GD.getInstance();
                    Thread.sleep(waitTime);
                    if (GDSystem.isStandAloneMode()) {
                    }
                    splashScreen.setMessage("Configurando Gr�ficos");
                    splashScreen.raiseBar();
                    configureShell(shell);
                    Thread.sleep(waitTime);
                    splashScreen.setMessage("Criando Componentes");
                    splashScreen.raiseBar();
                    createContents(shell);
                    Thread.sleep(waitTime);
                    splashScreen.setMessage("Sistema pronto");
                    splashScreen.raiseBar();
                    Thread.sleep(waitTime);
                    shell.open();
                } catch (Throwable e) {
                }
            }
        });
    }

    public Shell getShell() {
        return shell;
    }

    public void close() {
        shell.close();
    }

    public void configureShell(final Shell shell) {
        shell.setLayout(new FormLayout());
        shell.setText(Constants.VERSION);
        shell.setSize(1024, 768);
        shell.setMaximized(true);
        image = new Image(null, "img/at.png");
        shell.setImage(image);
    }

    protected Control createContents(Composite parent) {
        createMainPanel(parent);
        createStatusPanel(parent);
        createMainMenu();
        createCTabFolder();
        createWindowControl();
        openLoginScreen();
        getShell().getDisplay().asyncExec(new Runnable() {

            public void run() {
                if (!system.isConfigured()) windowControl.openConfigureWizard();
            }
        });
        return parent;
    }

    private void createWindowControl() {
        windowControl = GDWindowControl.getInstance();
        windowControl.setCTabFolder(cTabFolder);
        windowControl.setMainScreen(this);
    }

    private void createCTabFolder() {
        cTabFolder = new CTabFolder(mainPanel, SWT.CLOSE | SWT.MULTI | SWT.TOP | SWT.BORDER | SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.LEFT_TO_RIGHT);
        cTabFolder.setSimple(false);
        cTabFolder.setTabHeight(24);
        cTabFolder.setSelectionBackground(new Color[] { Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND), Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT) }, new int[] { 90 }, true);
        cTabFolder.setSelectionForeground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_FOREGROUND));
    }

    private void createInicialConfigurationCTabFolder() {
        inicialConfigurationcTabFolder = new CTabFolder(mainPanel, SWT.MULTI | SWT.TOP | SWT.BORDER | SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.LEFT_TO_RIGHT);
        inicialConfigurationcTabFolder.setSimple(false);
        inicialConfigurationcTabFolder.setTabHeight(24);
        inicialConfigurationcTabFolder.setSelectionBackground(new Color[] { Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND), Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT) }, new int[] { 90 }, true);
        inicialConfigurationcTabFolder.setSelectionForeground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_FOREGROUND));
        mainPanel.setWeights(new int[] { 3, 1 });
    }

    public CTabFolder getTabFolder() {
        return cTabFolder;
    }

    private void createMainPanel(Composite parent) {
        mainPanel = new SashForm(parent, SWT.NONE);
        FormData data = new FormData();
        data.top = new FormAttachment(0, 5);
        data.left = new FormAttachment(0, 5);
        data.right = new FormAttachment(100, -5);
        data.bottom = new FormAttachment(100, -25);
        mainPanel.setLayoutData(data);
    }

    private void createStatusPanel(Composite parent) {
        statusPanel = new SashForm(parent, SWT.NONE);
        FormData data = new FormData();
        data.top = new FormAttachment(mainPanel, 4);
        data.left = new FormAttachment(0, 5);
        data.right = new FormAttachment(100, -5);
        data.bottom = new FormAttachment(100, 0);
        statusPanel.setLayoutData(data);
        statusMessage = new Label(statusPanel, SWT.LEFT);
        statusMessage.setText(Constants.VERSION);
    }

    public void setStatusMessage(String message) {
        statusMessage.setText(message);
    }

    private void createMainMenu() {
        menu = new Menu(getShell(), SWT.BAR);
        createFileMenu(menu);
        if (system.isAuthenticated()) {
            createRegisterMenu(menu);
            createFinancialInfoMenu(menu);
            createReportMenu(menu);
        }
        createHelpMenu(menu);
        getShell().setMenuBar(menu);
    }

    private void createFinancialInfoMenu(Menu menu) {
        MenuItem financialInfoMenuItem = new MenuItem(menu, SWT.CASCADE);
        financialInfoMenuItem.setText("Finan�as");
        Menu financialMenu = new Menu(menu);
        financialInfoMenuItem.setMenu(financialMenu);
        if (system.hasAccess("Caixa") && system.getRegisterMachine() != null) {
            MenuItem accountItem = new MenuItem(financialMenu, SWT.NONE);
            accountItem.setText("Caixa");
            accountItem.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    windowControl.openRegisterMachineControlCenter();
                }
            });
        }
        if (system.hasAccess("Despesas / Receitas")) {
            MenuItem accountsToPayItem = new MenuItem(financialMenu, SWT.NONE);
            accountsToPayItem.setText("Despesas e Receitas");
            accountsToPayItem.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    windowControl.openExpendituresAndImcomingControlCenter();
                }
            });
        }
        if (system.hasAccess("Fluxo de Caixa")) {
            MenuItem accountsFlowItem = new MenuItem(financialMenu, SWT.NONE);
            accountsFlowItem.setText("Fluxo de Caixa");
            accountsFlowItem.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    windowControl.openCashFlowControlCenter();
                }
            });
        }
        if (system.hasAccess("Confer�ncia")) {
            MenuItem accountsConferScreen = new MenuItem(financialMenu, SWT.NONE);
            accountsConferScreen.setText("Confer�ncia");
            accountsConferScreen.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    windowControl.openManagerConferScreenControlCenter();
                }
            });
        }
        if (system.hasAccess("Contas")) {
            MenuItem accountControlCenter = new MenuItem(financialMenu, SWT.NONE);
            accountControlCenter.setText("Contas");
            accountControlCenter.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    windowControl.openAccountControlCenter();
                }
            });
        }
        if (system.hasAccess("Controle de Cheques")) {
            MenuItem checkControlCenter = new MenuItem(financialMenu, SWT.NONE);
            checkControlCenter.setText("Controle de Cheques");
            checkControlCenter.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    windowControl.openCheckControlCenter();
                }
            });
        }
        if (system.hasAccess("Importa��o de Extrato")) {
            MenuItem importExtractBankDialog = new MenuItem(financialMenu, SWT.NONE);
            importExtractBankDialog.setText("Importa��o de Extrato Banc�rio");
            importExtractBankDialog.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    windowControl.openLinkAccountBankControlCenter();
                }
            });
        }
        if (financialMenu.getItemCount() == 0) financialInfoMenuItem.dispose();
    }

    private void createRegisterMenu(Menu menu) {
        MenuItem registerMenuItem = new MenuItem(menu, SWT.CASCADE);
        registerMenuItem.setText("Centrais de Controle");
        Menu registerMenu = new Menu(menu);
        registerMenuItem.setMenu(registerMenu);
        if (system.hasAccess("Central de Alunos")) {
            MenuItem registerItem = new MenuItem(registerMenu, SWT.NONE);
            registerItem.setText("Central de Alunos");
            registerItem.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    windowControl.openStudentScreen();
                }
            });
        }
        if (system.hasAccess("Central de Funcionarios")) {
            MenuItem employeeItem = new MenuItem(registerMenu, SWT.NONE);
            employeeItem.setText("Central de Funcion�rios");
            employeeItem.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    windowControl.openEmployeeScreen();
                }
            });
        }
        if (system.hasAccess("Central Financeira")) {
            MenuItem finantialItem = new MenuItem(registerMenu, SWT.NONE);
            finantialItem.setText("Central Financeira");
            finantialItem.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    windowControl.openFinantialScreen();
                }
            });
        }
        new MenuItem(registerMenu, SWT.SEPARATOR);
        if (system.hasAccess("Central de Salas")) {
            MenuItem classRoomItem = new MenuItem(registerMenu, SWT.NONE);
            classRoomItem.setText("Central de Salas");
            classRoomItem.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    windowControl.openClassRoomScreen();
                }
            });
        }
        if (system.hasAccess("Central de Cursos")) {
            MenuItem courseItem = new MenuItem(registerMenu, SWT.NONE);
            courseItem.setText("Central de Cursos");
            courseItem.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    windowControl.openCourseControlCenter();
                }
            });
        }
        if (system.hasAccess("Central de Matricula")) {
            MenuItem registerItem = new MenuItem(registerMenu, SWT.NONE);
            registerItem.setText("Central de Matricula");
            registerItem.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    windowControl.openRegistrationControlCenter();
                }
            });
        }
        if (system.hasAccess("Central de Pesquisa")) {
            MenuItem searchItem = new MenuItem(registerMenu, SWT.NONE);
            searchItem.setText("Central de Pesquisa");
            searchItem.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    windowControl.openSearchControlCenter();
                }
            });
        }
        if (system.hasAccess("Central de Seguran�a")) {
            MenuItem searchItem = new MenuItem(registerMenu, SWT.NONE);
            searchItem.setText("Central de Seguran�a");
            searchItem.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    windowControl.openSecurityScreen();
                }
            });
        }
        if (system.hasAccess("Central de Hor�rios")) {
            MenuItem searchItem = new MenuItem(registerMenu, SWT.NONE);
            searchItem.setText("Central de Hor�rios");
            searchItem.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    windowControl.openRegistrationMapControlCenter();
                }
            });
        }
        if (registerMenu.getItemCount() <= 1) {
            registerMenuItem.dispose();
        }
    }

    private void createReportMenu(Menu menu) {
        MenuItem reportMenuItem = new MenuItem(menu, SWT.CASCADE);
        reportMenuItem.setText("Relat�rio");
        Menu reportMenu = new Menu(menu);
        reportMenuItem.setMenu(reportMenu);
        if (system.hasAccess("Extrato de Contas") || system.hasAccess("Holerite") || system.hasAccess("Gera��o de Boletos") || system.hasAccess("Controle de Cheques") || system.hasAccess("Relat�rio de Parcelas")) {
            MenuItem financialMenuItem = new MenuItem(reportMenu, SWT.CASCADE);
            financialMenuItem.setText("Financeiro");
            Menu financialMenu = new Menu(reportMenu);
            financialMenuItem.setMenu(financialMenu);
            if (system.hasAccess("Extrato de Contas")) {
                MenuItem dailyAccountItem = new MenuItem(financialMenu, SWT.NONE);
                dailyAccountItem.setText("Extrato de Contas Caixa");
                dailyAccountItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openExtractAccountReportControlCenter();
                    }
                });
            }
            if (system.hasAccess("Holerite")) {
                MenuItem simpleEmployeePaymentItem = new MenuItem(financialMenu, SWT.NONE);
                simpleEmployeePaymentItem.setText("Holerite");
                simpleEmployeePaymentItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openHoleriteControlCenter();
                    }
                });
            }
            if (system.hasAccess("Gera��o de Boletos")) {
                MenuItem registerItem = new MenuItem(financialMenu, SWT.NONE);
                registerItem.setText("Gera��o de Boletos");
                registerItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openBankingBilletReportControlCenter();
                    }
                });
            }
            if (system.hasAccess("Gera��o de Carn�s")) {
                MenuItem registerItem = new MenuItem(financialMenu, SWT.NONE);
                registerItem.setText("Gera��o de Carn�s");
                registerItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openPaymentBookReportControlCenter();
                    }
                });
            }
            if (system.hasAccess("Controle de Cheques")) {
                MenuItem registerItem = new MenuItem(financialMenu, SWT.NONE);
                registerItem.setText("Controle de Cheques");
                registerItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openCheckControlCenter();
                    }
                });
            }
            if (system.hasAccess("Carta de Cobran�a")) {
                MenuItem registerItem = new MenuItem(financialMenu, SWT.NONE);
                registerItem.setText("Carta de Cobran�a");
                registerItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openPaymentLetterReportControlCenter();
                    }
                });
            }
            if (system.hasAccess("Relat�rio de Parcelas")) {
                MenuItem registrationReportItem = new MenuItem(financialMenu, SWT.NONE);
                registrationReportItem.setText("Relat�rio de Parcelas");
                registrationReportItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openParcelReportControlCenter();
                    }
                });
            }
            if (system.hasAccess("Contas � Pagar/Receber")) {
                MenuItem registerItem = new MenuItem(financialMenu, SWT.NONE);
                registerItem.setText("Contas � Pagar/Receber");
                registerItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openExpendituresAndIncomingsReport();
                    }
                });
            }
        }
        if (system.hasAccess("Relat�rio de Presen�as") || system.hasAccess("Contrato em Branco") || system.hasAccess("Relat�rio de Alunos") || system.hasAccess("Ficha de Cadastro") || system.hasAccess("Quadro de Hor�rios") || system.hasAccess("Certificados")) {
            MenuItem registrationMenuItem = new MenuItem(reportMenu, SWT.CASCADE);
            registrationMenuItem.setText("Matr�cula");
            Menu registrationMenu = new Menu(reportMenu);
            registrationMenuItem.setMenu(registrationMenu);
            if (system.hasAccess("Relat�rio de Presen�as")) {
                MenuItem blanckContractItem = new MenuItem(registrationMenu, SWT.NONE);
                blanckContractItem.setText("Relat�rio de Presen�as");
                blanckContractItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openRegistrationAppointmentControlCenter();
                    }
                });
            }
            if (system.hasAccess("Relat�rio de Alunos Faltante por Data e Hor�rio")) {
                MenuItem blanckContractItem = new MenuItem(registrationMenu, SWT.NONE);
                blanckContractItem.setText("Relat�rio de Alunos Faltante por Data e Hor�rio");
                blanckContractItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openStudentMissedClassesControlCenter();
                    }
                });
            }
            if (system.hasAccess("Contrato em Branco")) {
                MenuItem blanckContractItem = new MenuItem(registrationMenu, SWT.NONE);
                blanckContractItem.setText("Contrato em Branco");
                blanckContractItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openBlankContract();
                    }
                });
            }
            if (system.hasAccess("Relat�rio de Alunos")) {
                MenuItem studentRegistrationReportItem = new MenuItem(registrationMenu, SWT.NONE);
                studentRegistrationReportItem.setText("Relat�rio de Alunos");
                studentRegistrationReportItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openStudentRegistrationReportControlCenter();
                    }
                });
            }
            if (system.hasAccess("Ficha de Cadastro")) {
                MenuItem registrationFiche = new MenuItem(registrationMenu, SWT.NONE);
                registrationFiche.setText("Ficha de Cadastro");
                registrationFiche.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openRegistrationFiche();
                    }
                });
            }
            if (system.hasAccess("Quadro de Hor�rios")) {
                MenuItem registerItem = new MenuItem(registrationMenu, SWT.NONE);
                registerItem.setText("Quadro de Hor�rios");
                registerItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openRegistrationMapReportControlCenter();
                    }
                });
            }
            if (system.hasAccess("Impress�o de Certificado")) {
                MenuItem certifydReportItem = new MenuItem(registrationMenu, SWT.NONE);
                certifydReportItem.setText("Impress�o de Certificado");
                certifydReportItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openCertifydReportControlCenter();
                    }
                });
            }
            if (system.hasAccess("Certificados Pendentes para Impress�o")) {
                MenuItem studentWithoutPrintedCertifyReportItem = new MenuItem(registrationMenu, SWT.NONE);
                studentWithoutPrintedCertifyReportItem.setText("Certificados Pendentes para Impress�o");
                studentWithoutPrintedCertifyReportItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openStudentWithoutPrintedCertifydReportControlCenter();
                    }
                });
            }
        }
        if (system.hasAccess("Aniversariantes do M�s") || system.hasAccess("Carta de boas-vindas") || system.hasAccess("Etiquetas")) {
            MenuItem severalsMenuItem = new MenuItem(reportMenu, SWT.CASCADE);
            severalsMenuItem.setText("Diversos");
            Menu severalMenu = new Menu(reportMenu);
            severalsMenuItem.setMenu(severalMenu);
            if (system.hasAccess("Aniversariantes do M�s")) {
                MenuItem studentBirthDayReportItem = new MenuItem(severalMenu, SWT.NONE);
                studentBirthDayReportItem.setText("Aniversariantes do M�s");
                studentBirthDayReportItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openBirthdayReport();
                    }
                });
            }
            if (system.hasAccess("Carta de boas-vindas")) {
                MenuItem studentBirthDayReportItem = new MenuItem(severalMenu, SWT.NONE);
                studentBirthDayReportItem.setText("Carta de boas-vindas");
                studentBirthDayReportItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openWelcomeReport();
                    }
                });
            }
            if (system.hasAccess("Etiquetas")) {
                MenuItem studentBirthDayReportItem = new MenuItem(severalMenu, SWT.NONE);
                studentBirthDayReportItem.setText("Impress�o de Etiquetas");
                studentBirthDayReportItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openLabelMailingReport();
                    }
                });
            }
        }
        if (reportMenu.getItemCount() == 0) reportMenuItem.dispose();
    }

    private void createFileMenu(Menu menu) {
        MenuItem fileItem = new MenuItem(menu, SWT.CASCADE);
        fileItem.setText("Arquivo");
        Menu fileMenu = new Menu(menu);
        fileItem.setMenu(fileMenu);
        if (system.isAuthenticated()) {
            MenuItem homeItem = new MenuItem(fileMenu, SWT.NONE);
            homeItem.setText("Central de Controle\tCtrl+Shift+C");
            homeItem.setAccelerator(SWT.CTRL | SWT.SHIFT | 'c');
            homeItem.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    openHomeScreen();
                }
            });
            MenuItem openStudentItem = new MenuItem(fileMenu, SWT.NONE);
            openStudentItem.setText("Abrir Aluno\tCtrl+Shift+A");
            openStudentItem.setAccelerator(SWT.CTRL | SWT.SHIFT | 'a');
            openStudentItem.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    new LoadStudentDialog(getShell()).open();
                }
            });
            new MenuItem(fileMenu, SWT.SEPARATOR);
            if (system.hasAccess("Configura��es")) {
                MenuItem configItem = new MenuItem(fileMenu, SWT.NONE);
                configItem.setText("Configura��es");
                configItem.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        new ShowFieldPrefs().run();
                    }
                });
            }
            if (system.hasAccess("Backup") && GDSystem.isStandAloneMode()) {
                MenuItem backup = new MenuItem(fileMenu, SWT.NONE);
                backup.setText("Backup");
                backup.addListener(SWT.Selection, new Listener() {

                    public void handleEvent(Event e) {
                        windowControl.openConfigureBackupWizard();
                    }
                });
            }
            new MenuItem(fileMenu, SWT.SEPARATOR);
            MenuItem logoutItem = new MenuItem(fileMenu, SWT.NONE);
            logoutItem.setText("Logout");
            logoutItem.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event e) {
                    logout();
                }
            });
        }
        MenuItem exitItem = new MenuItem(fileMenu, SWT.NONE);
        exitItem.setText("Sair");
        exitItem.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                close();
            }
        });
    }

    private void createHelpMenu(Menu menu) {
        MenuItem helpItem = new MenuItem(menu, SWT.CASCADE);
        helpItem.setText("Ajuda");
        Menu helpMenu = new Menu(menu);
        helpItem.setMenu(helpMenu);
        MenuItem updatesItem = new MenuItem(helpMenu, SWT.NONE);
        updatesItem.setText("Procurar por Atualiza��es");
        updatesItem.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                windowControl.updateDialog();
            }
        });
        new MenuItem(helpMenu, SWT.SEPARATOR);
        MenuItem homeItem = new MenuItem(helpMenu, SWT.NONE);
        homeItem.setText("Sobre o Gerente Digital");
        homeItem.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                windowControl.openAboutDialog();
            }
        });
    }

    public void logout() {
        system.logout();
        CTabItem[] items = cTabFolder.getItems();
        for (CTabItem item : items) {
            item.dispose();
        }
        closeInicialConfiguration();
        openLoginScreen();
    }

    public void closeView(ControlCenter controlCenter) {
        if (mainPanel != null) {
            menu.dispose();
            mainPanel.redraw();
            controlCenter.dispose();
            createMainMenu();
        }
    }

    protected void closeView(Composite screen) {
        if (mainPanel != null) {
            menu.dispose();
            mainPanel.redraw();
            screen.dispose();
            createMainMenu();
        }
    }

    protected void openHomeScreen() {
        GDWindowControl.getInstance().openHomeScreen();
        menu.dispose();
        createMainMenu();
    }

    public void openLoginScreen() {
        menu.dispose();
        windowControl.openLoginScreen();
        createMainMenu();
    }

    @SuppressWarnings("unchecked")
    public void openInicialConfiguration() {
        if (mainPanel != null) {
            try {
                Map<String, Account> accounts = (Map<String, Account>) system.query(new GetAccounts());
                Map<String, Person> employee = (Map<String, Person>) system.query(new GetEmployee());
                Map<String, ClassRoom> classRoom = (Map<String, ClassRoom>) system.query(new GetClassRoom());
                Map<String, Course> courses = (Map<String, Course>) system.query(new GetCourse());
                float contValueOfCourses = 0;
                for (Course course : courses.values()) {
                    contValueOfCourses += course.getValue();
                }
                if (employee.size() == 0 || accounts.size() == 0 || classRoom.size() == 0 || contValueOfCourses == 0) {
                    if (inicialConfigurationcTabFolder == null) {
                        createInicialConfigurationCTabFolder();
                    }
                    InicialConfiguration inicialConfiguration = new InicialConfiguration(inicialConfigurationcTabFolder, SWT.NONE);
                    inicialConfigurationcTabFolder.setSelection(inicialConfiguration);
                } else {
                    closeInicialConfiguration();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void closeInicialConfiguration() {
        if (inicialConfigurationcTabFolder != null) {
            inicialConfigurationcTabFolder.dispose();
            inicialConfigurationcTabFolder = null;
            mainPanel.layout();
        }
    }

    public void updateInicialConfigurationFolder() {
        if (inicialConfigurationcTabFolder != null) {
            CTabItem[] inicialConfigurationTabs = inicialConfigurationcTabFolder.getItems();
            for (int i = 0; i < inicialConfigurationTabs.length; i++) {
                inicialConfigurationTabs[i].dispose();
            }
        }
        openInicialConfiguration();
    }

    public void addEnterTraverse(Control control) {
        control.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent event) {
                if ((event.keyCode == 13) || (event.keyCode == 9) || (event.keyCode == 16777296)) {
                    event.doit = true;
                    event.detail = 16;
                    if (event.keyCode == 9 && event.stateMask != 0) {
                        event.detail = 8;
                    }
                    if (event.getSource() instanceof Text) {
                        Text text = (Text) event.getSource();
                        if (text.getStyle() == 33572930) if (event.keyCode == 13) {
                            event.doit = false;
                        }
                    }
                }
            }
        });
    }

    public void addEnterLastTraverse(Control control, final TabFolder tabFolder) {
        control.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent event) {
                if ((event.keyCode == 13) || (event.keyCode == 9) || (event.keyCode == 16777296)) {
                    event.doit = false;
                    Event leftKey = new Event();
                    leftKey.keyCode = 16777220;
                    tabFolder.notifyListeners(SWT.Traverse, leftKey);
                }
            }
        });
    }

    public void changeToFirstUpperCaseWhenLostFocus(final Text arg0) {
        arg0.addFocusListener(new FocusAdapter() {

            public void focusLost(FocusEvent event) {
                arg0.setText(new StringTool(arg0.getText()).toFirstUpperCase());
            }
        });
    }

    private void deleteDir(String strFile) {
        File fDir = new File(strFile);
        File[] strChildren = null;
        if (fDir.isDirectory()) {
            strChildren = fDir.listFiles();
            for (int i = 0; i < strChildren.length; i++) {
                strChildren[i].delete();
            }
        }
    }
}
