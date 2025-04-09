package org.jcompany.control.struts.service;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.el.PropertyNotFoundException;
import javax.persistence.DiscriminatorValue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.validator.DynaValidatorActionForm;
import org.jcompany.commons.PlcArgEntity;
import org.jcompany.commons.PlcBaseContextEntity;
import org.jcompany.commons.PlcBaseEntity;
import org.jcompany.commons.PlcConstantsCommons;
import org.jcompany.commons.PlcException;
import org.jcompany.commons.PlcConstantsCommons.ENTITY;
import org.jcompany.commons.annotation.PlcPrimaryKey;
import org.jcompany.commons.facade.IPlcFacade;
import org.jcompany.commons.helper.PlcAnnotationHelper;
import org.jcompany.commons.helper.PlcBeanCloneHelper;
import org.jcompany.commons.helper.PlcReflectionHelper;
import org.jcompany.commons.helper.PlcEntityHelper;
import org.jcompany.config.PlcConfigControlHelper;
import org.jcompany.config.PlcConfigHelper;
import org.jcompany.config.commons.PlcConfigSuffixClass;
import org.jcompany.config.control.collaboration.struts.PlcConfigStrutsHelper;
import org.jcompany.control.PlcConstants;
import org.jcompany.control.PlcControlLocator;
import org.jcompany.control.cache.PlcAggregatePropLocator;
import org.jcompany.control.cache.PlcCacheSessionEntity;
import org.jcompany.control.helper.PlcNaturalKeyHelper;
import org.jcompany.control.struts.PlcActionMapping;
import org.jcompany.control.struts.PlcActionMappingDet;
import org.jcompany.control.struts.PlcActionMappingSubDet;
import org.jcompany.control.struts.helper.PlcFormBeanAutomaticHelper;

/**
 * jCompany 3.0. Singleton. Utilit�rio para l�gicas de transforma��o de
 * informa�oes de Form-Bean para VOs e vice-versa. Encapula c�pias e regras
 * vinculadas � opera��o.
 * 
 * @author alvim, cl�udia seara
 * @since jCompany 3.0
 * @version $Id: PlcFormEntityService.java,v 1.22 2006/08/23 15:08:41 bruno_grossi
 *          Exp $
 */
public class PlcFormEntityService {

    protected static Logger log = Logger.getLogger(PlcFormEntityService.class);

    /**
	 * @since jcompany5 Utilit�rios para a manipula��o de metadados via Anota��es
	 */
    protected static final PlcConfigStrutsHelper config = PlcConfigStrutsHelper.getInstance();

    /**
	 * jCompany 3.0 DP Composite. Devolve o singleton que encapsula l�gica de
	 * manipula��es de recursos de internacionaliza��o
	 * <p>
	 * Ao se pegar o servi�o de registro visual a partir deste m�todo e n�o
	 * instanci�-lo diretamente, cria-se um desacoplamento que permite que se
	 * altere este servi�o por outros espec�ficos, com m�nimo de esfor�o.
	 * 
	 * @return Servi�o de registro de manipula��es visuais.
	 */
    protected PlcI18nService getI18nService() throws PlcException {
        return (PlcI18nService) PlcControlLocator.getInstance().get(PlcI18nService.class);
    }

    /**
	 * jCompany 3.0 DP Composite. Devolve o singleton que encapsula l�gica de
	 * manipula�ao de Form-Bean
	 * <p>
	 * Ao se pegar o servi�o de registro visual a partir deste m�todo e n�o
	 * instanci�-lo diretamente, cria-se um desacoplamento que permite que se
	 * altere este servi�o por outros espec�ficos, com m�nimo de esfor�o.
	 * 
	 * @return Servi�o manipula��o de Form-Beans
	 */
    protected PlcFormBeanAutomaticHelper getFormBeanService() {
        return PlcFormBeanAutomaticHelper.getInstance();
    }

    /**
	 * jCompany 3.0 DP Composite. Devolve o singleton que encapsula l�gica de
	 * manipula��es de recursos de internacionaliza��o
	 * <p>
	 * Ao se pegar o servi�o de registro visual a partir deste m�todo e n�o
	 * instanci�-lo diretamente, cria-se um desacoplamento que permite que se
	 * altere este servi�o por outros espec�ficos, com m�nimo de esfor�o.
	 * 
	 * @return Servi�o de registro de manipula��es visuais.
	 */
    protected PlcValidationService getValidacaoService() throws PlcException {
        return (PlcValidationService) PlcControlLocator.getInstance().get(PlcValidationService.class);
    }

    /**
	 * JCompany: M�todo que torna mais documentado o acesso a objetos em escopo
	 * da sess�o, atrav�s de uma classe �nica.
	 */
    protected PlcCacheSessionEntity recuperaObjetoSessao(HttpServletRequest request) {
        return ((PlcCacheSessionEntity) request.getSession().getAttribute(PlcConstants.SESSION_CACHE_KEY));
    }

    /**
	 * JCompany: M�todo que torna mais documentada a grava��o objetos em escopo
	 * da sess�o, atrav�s de uma classe �nica
	 */
    protected void armazenaObjetoSessao(HttpServletRequest request, PlcCacheSessionEntity objeto) {
        request.getSession().setAttribute(PlcConstants.SESSION_CACHE_KEY, objeto);
    }

    /**
	 * JCompany: Transfere informa��es entre dois JavaBeans. Um deles deve ser
	 * do type AcionForm. Com este m�todo, o jCompany gera um ENTITY a partir do
	 * Form-Bean da Struts e vice-versa, eliminando l�gicas de->para sem
	 * express�o para os neg�cios. Obs: N�o foram utilizados os utiliat�rios
	 * jakarta para beans devido a limita��es
	 * 
	 * @param origem
	 *            bean de entrada
	 * @param destino
	 *            bean de saida
	 * @return Retorna null ou uma instancia da classe de chave primaria, se ela
	 *         for composta e for transferencia do Form-Bean para o ENTITY
	 */
    @SuppressWarnings("unchecked")
    public Object transfereBeans(PlcBaseContextEntity context, PlcActionMapping plcMapping, HttpServletRequest request, java.lang.Object destino, java.lang.Object origem, String sufixoDet) throws PlcException {
        Logger log = Logger.getLogger("transferebeans");
        Logger logCc = Logger.getLogger("transferebeansCc");
        log.debug("########## Entrou no transfereBeans");
        Method metodo = null;
        Class[] parametros = null;
        Object retObj = null;
        Class voClass = null;
        BeanInfo info = null;
        PropertyDescriptor[] pd = null;
        DynaProperty[] dynaProperty = null;
        String nomePropForm = "";
        try {
            if (destino instanceof DynaActionForm || destino instanceof DynaValidatorActionForm) {
                log.debug("###########TransfereBeans: ORIGEM vo ==> DESTINO form");
                dynaProperty = ((DynaActionForm) destino).getDynaClass().getDynaProperties();
                voClass = origem.getClass();
                info = Introspector.getBeanInfo(voClass);
                pd = info.getPropertyDescriptors();
                PlcBaseEntity voOrigem = (PlcBaseEntity) origem;
                for (int i = 0; i < pd.length; i++) {
                    PlcPrimaryKey annotation = (PlcPrimaryKey) voClass.getAnnotation(PlcPrimaryKey.class);
                    if ((plcMapping != null && plcMapping.getClassePrimaryKey() != null && !plcMapping.getClassePrimaryKey().equals("") && !plcMapping.getClassePrimaryKey().equals((String) config.get(plcMapping, PlcConstantsCommons.ENTITY_ROOT)) && pd[i].getName().equals("idComposto")) || (annotation != null && !annotation.classe().isAssignableFrom(voClass.getClass()) && pd[i].getName().equals("idComposto"))) {
                        logCc.debug("Entrou para copiar chave composta para o form-bean");
                        metodo = pd[i].getReadMethod();
                        parametros = metodo.getParameterTypes();
                        retObj = metodo.invoke(origem, (Object[]) parametros);
                        transfereBeans(context, plcMapping, request, destino, retObj, "");
                        continue;
                    }
                    for (int k = 0; k < dynaProperty.length; k++) {
                        nomePropForm = dynaProperty[k].getName();
                        if (log.isDebugEnabled()) log.debug("mapping=" + plcMapping + " prop" + nomePropForm);
                        if ((plcMapping == null || plcMapping.getDesprezaTransfereBeans().indexOf("," + nomePropForm + ",") == -1) && PlcAnnotationHelper.getInstance().isCopyable(pd[i])) {
                            if ((pd[i].getName() + sufixoDet).equals(nomePropForm) && !(Set.class.isAssignableFrom(pd[i].getPropertyType()))) {
                                transfereUmBeanVOParaForm(context, plcMapping, voOrigem, (DynaActionForm) destino, pd[i], sufixoDet, nomePropForm);
                            }
                        }
                    }
                }
                if (plcMapping.getComponentes() != null) transfereBeansComponenteDescendenteVOParaForm(context, plcMapping, plcMapping.getComponentes(), origem, ((DynaActionForm) destino));
            } else {
                log.debug("############ TransfereBeans: ORIGEM form ==> DESTINO vo  ");
                dynaProperty = ((DynaActionForm) origem).getDynaClass().getDynaProperties();
                voClass = destino.getClass();
                info = Introspector.getBeanInfo(voClass);
                pd = info.getPropertyDescriptors();
                for (int i = 0; i < dynaProperty.length; i++) {
                    for (int k = 0; k < pd.length; k++) {
                        if ((plcMapping == null || plcMapping.getDesprezaTransfereBeans().indexOf("," + pd[k].getName() + ",") == -1) && !PlcAnnotationHelper.getInstance().existsAnnotationToComponent(pd[k].getPropertyType())) {
                            if (dynaProperty[i].getName().equals(pd[k].getName())) {
                                log.debug(" transfereBeans: atributos iguais : form = " + dynaProperty[i].getName() + " vo = " + pd[k].getName());
                                if (transfereBeansFormParaVOEspecificoApi(context, plcMapping, request, (DynaActionForm) origem, dynaProperty[i], (PlcBaseEntity) destino, pd[k])) continue;
                                Object valorInformado = ((DynaActionForm) origem).get(dynaProperty[i].getName());
                                Object prop = null;
                                if (PropertyUtils.isReadable(destino, dynaProperty[i].getName())) prop = PropertyUtils.getProperty(destino, dynaProperty[i].getName());
                                if (!destino.getClass().getName().endsWith("KeyVO")) {
                                    boolean isAuxliar = dynaProperty[i].getName().endsWith("Str") || dynaProperty[i].getName().endsWith("Aux");
                                    boolean existeAux = ((DynaActionForm) origem).getMap().containsKey(dynaProperty[i].getName() + "Str") || ((DynaActionForm) origem).getMap().containsKey(dynaProperty[i].getName() + "Aux");
                                    String modoGravacao = (String) ((DynaActionForm) origem).get(PlcConstantsCommons.MODES.MODE);
                                    PlcBaseEntity voAnterior = montaVOAnterior(modoGravacao, request, voClass.getName());
                                    if (isAuxliar) {
                                        String propSemAuxliar = dynaProperty[i].getName().substring(0, dynaProperty[i].getName().length() - 3);
                                        String valorAtualSemAuxiliar = (String) (((DynaActionForm) origem).getMap().get(propSemAuxliar));
                                        if (voAnterior != null) {
                                            Object valorVoAnterior = null;
                                            try {
                                                valorVoAnterior = PropertyUtils.getProperty(voAnterior, propSemAuxliar);
                                            } catch (NoSuchMethodException e) {
                                            }
                                            Object valorVoAnteriorAux;
                                            if (valorVoAnterior != null && PlcBaseEntity.class.isAssignableFrom(valorVoAnterior.getClass())) valorVoAnteriorAux = ((PlcBaseEntity) valorVoAnterior).getIdAux(); else valorVoAnteriorAux = valorVoAnterior == null ? null : valorVoAnterior;
                                            Object valorAuxiliarVoAnterior = PropertyUtils.getProperty(voAnterior, dynaProperty[i].getName());
                                            String valorAuxiliarVoAnteriorStr;
                                            if (valorAuxiliarVoAnterior != null && PlcBaseEntity.class.isAssignableFrom(valorAuxiliarVoAnterior.getClass())) valorAuxiliarVoAnteriorStr = ((PlcBaseEntity) valorAuxiliarVoAnterior).getIdAux(); else valorAuxiliarVoAnteriorStr = valorAuxiliarVoAnterior == null ? "" : valorAuxiliarVoAnterior.toString();
                                            if (valorVoAnteriorAux != null && BigDecimal.class.isAssignableFrom(valorVoAnteriorAux.getClass())) {
                                                if ((valorVoAnteriorAux.toString().equals(valorAtualSemAuxiliar)) || (valorAuxiliarVoAnteriorStr.equals(valorInformado))) continue;
                                            } else if (valorVoAnteriorAux != null && Date.class.isAssignableFrom(valorVoAnteriorAux.getClass())) {
                                                Date data = null;
                                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                                formatter.setLenient(false);
                                                if (StringUtils.isNotBlank(valorAtualSemAuxiliar)) {
                                                    data = formatter.parse(valorAtualSemAuxiliar);
                                                }
                                                boolean isDiferente = data != null ? (!formatter.format(valorVoAnterior).equals(formatter.format(data))) : true;
                                                if (isDiferente || (valorAuxiliarVoAnteriorStr.equals(valorInformado))) continue;
                                            } else if ((valorVoAnteriorAux != null && valorVoAnteriorAux.toString().equals(valorAtualSemAuxiliar)) || (valorAuxiliarVoAnteriorStr != null && valorAuxiliarVoAnteriorStr.equals(valorInformado))) continue; else if (StringUtils.isNotBlank(valorAtualSemAuxiliar) && (valorAuxiliarVoAnterior == null || valorAuxiliarVoAnterior.equals(valorInformado))) continue;
                                        } else if (StringUtils.isNotBlank(valorAtualSemAuxiliar) && StringUtils.isBlank((String) valorInformado)) continue;
                                    } else if (existeAux) {
                                        String valorAux = (String) ((DynaActionForm) origem).getMap().get(dynaProperty[i].getName() + "Str");
                                        String valorNormal = (String) ((DynaActionForm) origem).getMap().get(dynaProperty[i].getName());
                                        if (voAnterior != null) {
                                            String valorAuxAnt = null;
                                            try {
                                                valorAuxAnt = (String) PropertyUtils.getProperty(voAnterior, dynaProperty[i].getName() + "Str");
                                            } catch (NoSuchMethodException e) {
                                            }
                                            Object valorNormalAnt = PropertyUtils.getProperty(voAnterior, dynaProperty[i].getName());
                                            if (StringUtils.isBlank(valorAuxAnt) && valorNormalAnt == null) {
                                                if (StringUtils.isNotBlank(valorAux)) continue;
                                            } else {
                                                if (StringUtils.isNotBlank(valorAux) && (valorAuxAnt == null || !valorAuxAnt.equals(valorAux))) continue; else if (valorInformado != null && StringUtils.isBlank(valorAux)) continue;
                                            }
                                        } else if (StringUtils.isNotBlank(valorAux)) continue;
                                    }
                                } else {
                                    String valorAux = (String) ((DynaActionForm) origem).getMap().get(dynaProperty[i].getName() + "Str");
                                    if (StringUtils.isNotBlank(valorAux)) continue;
                                }
                                PlcPrimaryKey annotation = null;
                                PlcAggregatePropLocator agregadoPropLocator = PlcAggregatePropLocator.getInstance();
                                Class classPropDestino = PropertyUtils.getPropertyType(destino, dynaProperty[i].getName());
                                if (PlcBaseEntity.class.isAssignableFrom(classPropDestino)) {
                                    PlcBaseEntity objClasse = agregadoPropLocator.getAggregatedClassObject(classPropDestino);
                                    if (objClasse != null) annotation = (PlcPrimaryKey) objClasse.getClass().getAnnotation(PlcPrimaryKey.class);
                                }
                                if (annotation == null && prop == null && (valorInformado == null || StringUtils.isBlank(valorInformado.toString())) && PlcBaseEntity.class.isAssignableFrom(classPropDestino)) continue;
                                if (prop != null && PlcBaseEntity.class.isAssignableFrom(prop.getClass()) && ((PlcBaseEntity) prop).getId() != null && ((PlcBaseEntity) prop).getIdAux().equals((String) valorInformado)) continue;
                                transfereUmBeanDoFormParaVO(pd[k], valorInformado, (DynaActionForm) origem, destino, nomePropForm);
                            }
                        }
                    }
                }
                if (plcMapping != null && plcMapping.getComponentes() != null) transfereBeansComponenteDescendenteFormParaVO(context, destino, plcMapping.getComponentes(), ((DynaActionForm) origem), nomePropForm);
                PlcPrimaryKey annotation = (PlcPrimaryKey) voClass.getAnnotation(PlcPrimaryKey.class);
                if ((plcMapping != null && plcMapping.getClassePrimaryKey() != null && !plcMapping.getClassePrimaryKey().equals("") && !plcMapping.getClassePrimaryKey().equals((String) config.get(plcMapping, PlcConstantsCommons.ENTITY_ROOT))) || (annotation != null && !annotation.classe().isAssignableFrom(voClass.getClass()))) {
                    log.debug("Entrou para copiar do form-bean para chave composta");
                    Object chaveNatural = null;
                    if (StringUtils.isNotBlank(plcMapping.getClassePrimaryKey())) {
                        chaveNatural = Class.forName(plcMapping.getClassePrimaryKey()).newInstance();
                    } else if (annotation != null) {
                        chaveNatural = annotation.classe().newInstance();
                    }
                    transfereBeans(context, null, request, chaveNatural, origem, "");
                    return chaveNatural;
                }
            }
            if (log.isDebugEnabled()) log.debug("NO VAI RETORNAR CHAVE NATURAL");
            return null;
        } catch (Exception ex) {
            throw new PlcException("jcompany.erros.transfereBeans", new Object[] { ex }, ex, log);
        }
    }

    /**
	 * Transfere informacoes de um ENTITY para um Form-Bean, convertendo para String.
	 */
    protected void transfereUmBeanVOParaForm(PlcBaseContextEntity context, PlcActionMapping plcMapping, Object voOrigem, DynaActionForm destino, PropertyDescriptor pd, String sufixoDet, String nomePropForm) throws PlcException {
        try {
            log.debug(" transfereBeans: atributos iguais : vo = " + pd.getName() + " form = " + nomePropForm);
            log.debug(" type do atributo do vo = " + pd.getPropertyType().getName());
            Method metodo = pd.getReadMethod();
            if (metodo == null) return;
            Object[] parametros = metodo.getParameterTypes();
            Object retObj = metodo.invoke(voOrigem, (Object[]) parametros);
            if (PlcConstantsCommons.PLC_CLASS_ENTITY.isAssignableFrom(pd.getPropertyType()) && !PlcAnnotationHelper.getInstance().existsAnnotationToComponent(pd.getPropertyType())) {
                if (sufixoDet.equals("")) transfereClasseAgregadaParaFormBean(context, plcMapping, (PlcBaseEntity) voOrigem, retObj, destino, nomePropForm);
            } else {
                log.debug(" transfereBeans: CLASSE JAVA ");
                log.debug(" transfereBeans: ANTES  copyProperties ");
                if (pd.getPropertyType().getName().indexOf("java.util.Date") >= 0) {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    if (retObj != null) {
                        String data = formatter.format(retObj);
                        int aux = data.indexOf("00:00");
                        if (aux > -1) data = data.substring(0, aux - 1);
                        if (log.isDebugEnabled()) log.debug(" transfereBeans: data = " + data);
                        BeanUtils.copyProperty(destino, nomePropForm, data);
                    } else {
                        log.debug("Esta colocando vazio na data porque o valor no ENTITY eh nulo!!");
                        BeanUtils.copyProperty(destino, nomePropForm, "");
                    }
                } else {
                    try {
                        if (!nomePropForm.equals("idNatural")) {
                            BeanUtils.copyProperty(destino, nomePropForm, retObj);
                        } else {
                            PlcPrimaryKey chavePrimaria = voOrigem.getClass().getAnnotation(PlcPrimaryKey.class);
                            if (chavePrimaria != null) {
                                String[] propriedades = chavePrimaria.properties();
                                for (String prop : propriedades) {
                                    if (PropertyUtils.isReadable(retObj, prop)) {
                                        Object valorPropKeyVO = PropertyUtils.getProperty(retObj, prop);
                                        BeanUtils.copyProperty(destino, prop, valorPropKeyVO);
                                    }
                                    if (PropertyUtils.isReadable(retObj, prop + "Str")) {
                                        Object valorPropKeyVO = PropertyUtils.getProperty(retObj, prop + "Str");
                                        BeanUtils.copyProperty(destino, prop + "Str", valorPropKeyVO);
                                    } else if (PropertyUtils.isReadable(retObj, prop + "Aux")) {
                                        Object valorPropKeyVO = PropertyUtils.getProperty(retObj, prop + "Aux");
                                        BeanUtils.copyProperty(destino, prop + "Aux", valorPropKeyVO);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.info("Falha controlada ao tentar copiar propriedade " + nomePropForm);
                    }
                }
            }
        } catch (Exception e) {
            throw new PlcException("jcompany.error.generic", new Object[] { "transfereUmBeanVOParaForm", e }, e, log);
        }
    }

    /**
	 * Transfere um valor do Form para o ENTITY
	 * 
	 * @param pd
	 *            Propriedade contendo o Valor
	 * @param valorInformado
	 *            Valor informado na propriedade
	 * @param destino
	 *            Vo de Destino
	 */
    protected void transfereUmBeanDoFormParaVO(PropertyDescriptor pd, Object valorInformado, DynaActionForm form, Object destino, String nomePropForm) throws PlcException {
        try {
            if (PlcBaseEntity.class.isAssignableFrom(pd.getPropertyType())) {
                Method metodo = pd.getWriteMethod();
                Object retObj = criaClasseAgregada(form, pd, valorInformado, nomePropForm);
                Object[] objValor = new Object[1];
                objValor[0] = retObj;
                metodo.invoke(destino, objValor);
            } else if (Enum.class.isAssignableFrom(pd.getPropertyType())) {
                Class classe = pd.getPropertyType();
                if (valorInformado == null || "".equals(valorInformado.toString().trim())) PropertyUtils.setProperty(destino, pd.getName(), null); else PropertyUtils.setProperty(destino, pd.getName(), Enum.valueOf(classe, (String) valorInformado));
            } else {
                if (Date.class.isAssignableFrom(pd.getPropertyType())) {
                    if (log.isDebugEnabled()) log.debug(" transfereBeans: type igual a Date - valor = " + valorInformado);
                    if (!pd.getName().equalsIgnoreCase("dataUltAlteracao")) {
                        if (valorInformado != null && !valorInformado.equals("")) {
                            String valorInformadoAux = valorInformado.toString();
                            if (StringUtils.countMatches(valorInformadoAux, ":") == 2) {
                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                log.debug(" transfereBeans: ANTES  copyProperties para DATE COM HORA/MINUTO/SEGUNDO");
                                BeanUtils.copyProperty(destino, pd.getName(), formatter.parse((String) valorInformado));
                            } else if (StringUtils.countMatches(valorInformadoAux, ":") == 1) {
                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                log.debug(" transfereBeans: ANTES  copyProperties para DATE COM HORA/MINUTO");
                                BeanUtils.copyProperty(destino, pd.getName(), formatter.parse((String) valorInformado));
                            } else {
                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                log.debug(" transfereBeans: ANTES  copyProperties para DATE SEM HORA");
                                BeanUtils.copyProperty(destino, pd.getName(), formatter.parse((String) valorInformado));
                            }
                        } else {
                            java.util.Date dataNula = null;
                            if (!pd.getName().equals("dataUltAlteracao")) BeanUtils.copyProperty(destino, pd.getName(), dataNula);
                        }
                    }
                } else if (Long.class.isAssignableFrom(pd.getPropertyType()) || BigDecimal.class.isAssignableFrom(pd.getPropertyType()) || Double.class.isAssignableFrom(pd.getPropertyType())) {
                    if (valorInformado == null || "".equals(valorInformado.toString().trim()) || !(NumberUtils.isNumber(valorInformado.toString()))) {
                        PropertyUtils.setProperty(destino, pd.getName(), null);
                    } else {
                        BeanUtils.copyProperty(destino, pd.getName(), valorInformado);
                    }
                } else if (!Integer.class.isAssignableFrom(pd.getPropertyType())) {
                    if (!pd.getName().equals("usuarioUltAlteracao") && !pd.getName().equals("versao") && !pd.getName().equals("detalhePlc_Det1") && (!pd.getName().equals(PlcConstants.FORM.AUTOMATION.FILE.ID_ATTACHED_FILE) || (pd.getName().equals(PlcConstants.FORM.AUTOMATION.FILE.ID_ATTACHED_FILE) && valorInformado != null && !valorInformado.toString().equals("") && !valorInformado.toString().equals("0")))) {
                        try {
                            if ((Set.class.isAssignableFrom(pd.getPropertyType()) || List.class.isAssignableFrom(pd.getPropertyType()) && valorInformado != null && valorInformado.toString().equals(""))) return;
                            if (log.isDebugEnabled()) log.debug("######## Vai tentar copiar " + pd.getName() + " do type" + pd.getPropertyType().getName() + " com valor " + valorInformado + " para " + destino);
                            if (PropertyUtils.isWriteable(destino, pd.getName())) BeanUtils.copyProperty(destino, pd.getName(), valorInformado); else if (log.isDebugEnabled()) log.debug("Nao copiou " + pd.getName() + " porque nao achou setter");
                        } catch (Exception e) {
                        }
                    } else log.debug("transfereBeans: nao copiou campo reservado");
                }
                log.debug(" transfereBeans: DEPOIS  copyProperties ");
            }
        } catch (Exception e) {
            throw new PlcException("jcompany.error.generic", new Object[] { "transfereUmBeanDoFormParaVO", e }, e, log);
        }
    }

    /**
	 * Transfere dados de todos os componentes ou descendentes (colecao de
	 * classes) informadas, do ENTITY para o FORM. Sempre parte do Componentes ou
	 * Descendentes, neste caso, para evitar varredura em todo o form-bean. TODO
	 * Varia��o de nomenclatura. Nesta versao, assume nome da classe sem Sufixos
	 * possivel "ENTITY" ou "Entity", em min�sculas.
	 * 
	 * @param context
	 *            Context
	 * @param listaClasses
	 *            Lista de componentes ou descendentes
	 */
    public void transfereBeansComponenteDescendenteVOParaForm(PlcBaseContextEntity context, PlcActionMapping plcMapping, Map<String, Class> compDescNomeProps, Object voOrigem, DynaActionForm f) throws PlcException {
        try {
            String propriedade = null;
            Class classeC = null;
            for (Iterator iter = compDescNomeProps.keySet().iterator(); iter.hasNext(); ) {
                propriedade = (String) iter.next();
                classeC = compDescNomeProps.get(propriedade);
                if (PropertyUtils.isReadable(voOrigem, propriedade)) {
                    Object compDesc = PropertyUtils.getProperty(voOrigem, propriedade);
                    if (compDesc != null) {
                        PropertyDescriptor[] pds;
                        try {
                            pds = Introspector.getBeanInfo(compDesc.getClass()).getPropertyDescriptors();
                        } catch (Exception e) {
                            throw new PlcException("jcompany.error.generic", new Object[] { "transfereBeansComponenteDescendente", e }, e, log);
                        }
                        for (int i = 0; i < pds.length; i++) {
                            transfereUmBeanVOParaForm(context, plcMapping, compDesc, f, pds[i], "", propriedade + "_" + pds[i].getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new PlcException("jcompany.error.generic", new Object[] { "transfereBeansComponenteDescendenteVOParaForm", e }, e, log);
        }
    }

    /**
	 * Transfere dados de todos os componentes ou descendentes (colecao de
	 * classes) informadas, do FORM para ENTITY. Sempre parte do Componentes ou
	 * Descendentes, neste caso, para evitar varredura em todo o form-bean. TODO
	 * Varia��o de nomenclatura. Nesta versao, assume nome da classe sem Sufixos
	 * possivel "ENTITY" ou "Entity", em min�sculas.
	 * 
	 * @param context
	 *            Context
	 * @param listaClasses
	 *            Lista de componentes ou descendentes
	 */
    protected void transfereBeansComponenteDescendenteFormParaVO(PlcBaseContextEntity context, Object voPrincipal, Map<String, Class> compDescNomeProps, DynaActionForm f, String nomePropForm) throws PlcException {
        try {
            String propriedade = null;
            Class classeC = null;
            for (Iterator iter = compDescNomeProps.keySet().iterator(); iter.hasNext(); ) {
                propriedade = (String) iter.next();
                classeC = compDescNomeProps.get(propriedade);
                Object compDesc = PlcEntityHelper.getInstance().createInstance(classeC.getName());
                PropertyDescriptor[] pds;
                try {
                    pds = Introspector.getBeanInfo(compDesc.getClass()).getPropertyDescriptors();
                } catch (Exception e) {
                    throw new PlcException("jcompany.error.generic", new Object[] { "transfereBeansComponenteDescendente", e }, e, log);
                }
                for (int i = 0; i < pds.length; i++) {
                    if (f.getDynaClass().getDynaProperty(propriedade + "_" + pds[i].getName()) != null) transfereUmBeanDoFormParaVO(pds[i], f.get(propriedade + "_" + pds[i].getName()), f, compDesc, nomePropForm);
                }
                PropertyUtils.setProperty(voPrincipal, propriedade, compDesc);
            }
        } catch (Exception e) {
            throw new PlcException("jcompany.error.generic", new Object[] { "transfereBeansComponenteDescendenteFormParaVO", e }, e, log);
        }
    }

    /**
	 * jCompany 3.0 Permite que se especializa a c�pia de uma propriedade
	 * espec�fica do Form-Bean para o ENTITY, realizando convers�es apropriadas.
	 * Para tanto, deve-se realizar a convers�o e retornar true para que o
	 * ancestral sobreponha a convers�o realizada
	 * 
	 * @param context
	 * @param plcMapping
	 * @param request
	 * @param form
	 *            Form-Bean de Origem
	 * @param propOrigemForm
	 *            Propriedade do form-bean de origem
	 * @param baseVO
	 *            ENTITY de Destino
	 * @param propDestinoVO
	 *            Propriedade do ENTITY de destino
	 * @return true para que o ancestral nao realize a conversao corrente e
	 *         false para que realize
	 */
    protected boolean transfereBeansFormParaVOEspecificoApi(PlcBaseContextEntity context, PlcActionMapping plcMapping, HttpServletRequest request, DynaActionForm form, DynaProperty propOrigemForm, PlcBaseEntity baseVO, PropertyDescriptor propDestinoVO) throws PlcException {
        return false;
    }

    /**
	 * jCompany 3.0 Permite que se especialize a c�pia de propriedade do ENTITY para
	 * form-bean dinamico. Se for realizada a convers�o, deve retornar true para
	 * que o ancestral nao a sobreponha.
	 * 
	 * @param voOrigem
	 *            Value Object de origem
	 * @param propOrigemVO
	 *            Propriedade de origem
	 * @param form
	 *            Form de destino
	 * @param propDestinoForm
	 *            Propriedade do form
	 * @return true para que o ancestral nao realize a conversao corrente e
	 *         false para que realize
	 */
    protected boolean transfereBeansVOParaFormEspecificoApi(PlcBaseContextEntity context, PlcActionMapping plcMapping, HttpServletRequest request, PlcBaseEntity voOrigem, PropertyDescriptor propOrigemVO, DynaActionForm form, DynaProperty propDestinoForm) throws PlcException {
        return false;
    }

    /**
	 * jCompany 2.7.3. Transfere dados de classe descendente de PlcBaseEntity,
	 * agregada, para form-bean
	 * 
	 * @param plcMapping
	 */
    private void transfereClasseAgregadaParaFormBean(PlcBaseContextEntity context, PlcActionMapping plcMapping, PlcBaseEntity voOrigem, Object classeAgregadaOrigem, DynaActionForm formDestino, String propriedadeAgregada) throws Exception {
        log.debug("######## Entrou em transfereClasseAgregada");
        if (classeAgregadaOrigem != null) {
            Class classObj = classeAgregadaOrigem.getClass();
            PlcPrimaryKey chavePrimaria = (PlcPrimaryKey) classObj.getAnnotation(PlcPrimaryKey.class);
            Object objFilho = null;
            if (chavePrimaria != null) {
                PlcBaseEntity idNatural = ((PlcBaseEntity) classeAgregadaOrigem).getDynamicNaturalId();
                String[] propriedades = chavePrimaria.properties();
                for (String prop : propriedades) {
                    Object valorPropId = PropertyUtils.getProperty(idNatural, prop);
                    formDestino.set(propriedadeAgregada + "_" + prop, valorPropId.toString());
                }
                objFilho = idNatural;
            } else {
                Method met = classObj.getMethod("getId", new Class[0]);
                objFilho = met.invoke(classeAgregadaOrigem, new Object[0]);
                formDestino.set(propriedadeAgregada, objFilho == null ? "" : objFilho.toString());
            }
            if (PropertyUtils.isReadable(voOrigem, PlcConstantsCommons.ATTRIBUT_AGGREGATE_LAZY) && context.isUsesRetrieveAggregatedClass()) {
                PlcAggregatePropLocator agregadoPropLocator = PlcAggregatePropLocator.getInstance();
                Map<String, PlcBaseEntity> ret = agregadoPropLocator.getAggregatedClassPropertyName(voOrigem, (Class<? extends PlcBaseEntity>) classeAgregadaOrigem.getClass());
                String agregadoLookupArg = plcMapping.getAgregadoLookupArg();
                try {
                    for (String nomeProp : ret.keySet()) {
                        if (nomeProp.equals(propriedadeAgregada)) {
                            String nomePropAgregada = nomeProp + PlcConstants.FORM.AUTOMATION.LOOKUP;
                            if (formDestino.getDynaClass().getDynaProperty(nomePropAgregada) != null) {
                                if ((objFilho == null && classeAgregadaOrigem.toString() == null) || (objFilho != null && classeAgregadaOrigem.toString() != null)) formDestino.set(nomePropAgregada, classeAgregadaOrigem.toString());
                            }
                            if (agregadoLookupArg != null && agregadoLookupArg.trim().length() > 0) {
                                Pattern p = Pattern.compile(nomeProp + "_([^,\\s]+)");
                                Matcher m = p.matcher(agregadoLookupArg);
                                if (m.find()) {
                                    String nomePropRecuperacaoForm = m.group(0);
                                    String nomePropRecuperacao = m.group(1);
                                    if (formDestino.getDynaClass().getDynaProperty(nomePropRecuperacaoForm) != null) {
                                        Object valor = PropertyUtils.getProperty(classeAgregadaOrigem, nomePropRecuperacao);
                                        if ((objFilho == null && valor == null) || (objFilho != null && valor != null)) formDestino.set(nomePropRecuperacaoForm, valor);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    /**
	 * JCompany. Para cada argumento de recupera��o informado, transfere as
	 * informa��es associadas a ele para um POJO e disponibiliza em um List.
	 * Deste modo, pode-se ter argumentos com propriedades duplicadas (ex:
	 * data_ArgINI e data_ArgFIM), algo que utilizando o ENTITY exigiria cria��o de
	 * propriedades transientes.
	 * 
	 * @param form
	 *            form de entrada de dados (deve ser do type DynaActionForm ou
	 *            DynaValidatorActionForm)
	 * @return um <code>List</code> com todos os arguementos de de recupera��o
	 *         informados
	 */
    public List<PlcArgEntity> transfereBeansArg(PlcActionMapping plcMapping, HttpServletRequest request, ActionForm form) throws PlcException {
        log.debug("################# Entrou transfereBeansArg");
        ArrayList vo = new ArrayList();
        String nomeAtributo = "";
        StringBuffer nomeFinalAtributo = new StringBuffer();
        Object valorAtributo = new Object();
        try {
            if (plcMapping.getArgPropriedade() != null) {
                log.debug("Entrou para montar propriedades");
                List argPropriedade = plcMapping.getArgPropriedade();
                List argOperador = plcMapping.getArgOperador();
                List argDescZero = plcMapping.getArgDescZero();
                List argFormato = plcMapping.getArgFormato();
                List argAlias = plcMapping.getArgAlias();
                List argOrIsNull = plcMapping.getArgOrIsNull();
                Iterator i = argPropriedade.iterator();
                int j = 0;
                String formato = "";
                String valorOper = "";
                while (i.hasNext()) {
                    nomeAtributo = (String) i.next();
                    if (log.isDebugEnabled()) log.debug("Entrou em loop para " + nomeAtributo + " j=" + j);
                    nomeFinalAtributo = new StringBuffer();
                    if (nomeAtributo.indexOf(PlcConstants.DEFAULT_LOGIC.CONSULTATION.QBE.QBE_IDT_ARG[4]) > -1) {
                        nomeFinalAtributo.append(nomeAtributo.substring(0, nomeAtributo.indexOf(PlcConstants.DEFAULT_LOGIC.CONSULTATION.QBE.QBE_IDT_ARG[4])));
                    } else if (nomeAtributo.indexOf(PlcConstants.DEFAULT_LOGIC.CONSULTATION.QBE.QBE_IDT_ARG[5]) > -1) {
                        nomeFinalAtributo.append(nomeAtributo.substring(0, nomeAtributo.indexOf(PlcConstants.DEFAULT_LOGIC.CONSULTATION.QBE.QBE_IDT_ARG[5])));
                    } else {
                        nomeFinalAtributo.append(nomeAtributo.substring(0, nomeAtributo.indexOf(PlcConstants.DEFAULT_LOGIC.CONSULTATION.QBE.QBE_IDT_ARG[0])));
                    }
                    if (log.isDebugEnabled()) log.debug("Nome do Atributo sem Arg =" + nomeFinalAtributo);
                    valorAtributo = ((DynaActionForm) form).get(nomeAtributo);
                    if (nomeAtributo.indexOf(PlcConstants.DEFAULT_LOGIC.CONSULTATION.QBE.QBE_IDT_ARG[3]) != -1) {
                        nomeFinalAtributo.append(PlcConstantsCommons.CONSULTATION.QBE.QBE_ATTR_SUFFIX_PHONETICS);
                        IPlcFacade facade = PlcControlLocator.getInstance().getFacadeDefault();
                        valorAtributo = facade.phonetics(valorAtributo.toString());
                    }
                    if (valorAtributo != null && !valorAtributo.equals("")) {
                        if (log.isDebugEnabled()) log.debug("\n transfereBeansArg: valorAtributo = " + valorAtributo.toString());
                        String desconsiderarValor = (String) argDescZero.get(j);
                        PlcArgEntity argVo = null;
                        if (!valorAtributo.toString().equals(desconsiderarValor)) {
                            argVo = new PlcArgEntity();
                            argVo.setType(PlcConstants.DEFAULT_LOGIC.CONSULTATION.QBE.QBE_TYPE_ARGUMENT);
                            argVo.setName(nomeFinalAtributo.toString());
                            argVo.setValue(valorAtributo.toString());
                            argVo.setAlias(argAlias.get(j) + "");
                            valorOper = (String) argOperador.get(j);
                            if (log.isDebugEnabled()) log.debug("ValorOper = " + valorOper);
                            if (valorOper.equals(PlcConstants.DEFAULT_LOGIC.CONSULTATION.QBE.QBE_LESS_THAN)) valorOper = "<"; else if (valorOper.equals(PlcConstants.DEFAULT_LOGIC.CONSULTATION.QBE.QBE_GREATER_THAN)) valorOper = ">"; else if (valorOper.equals(PlcConstants.DEFAULT_LOGIC.CONSULTATION.QBE.QBE_LESS_THAN_OR_EQUALS_TO)) valorOper = "<="; else if (valorOper.equals(PlcConstants.DEFAULT_LOGIC.CONSULTATION.QBE.QBE_GREATER_THAN_OR_EQUALS_TO)) valorOper = ">="; else if (valorOper.equals(PlcConstants.DEFAULT_LOGIC.CONSULTATION.QBE.QBE_EQUALS_TO)) valorOper = "="; else if (valorOper.equals(PlcConstants.DEFAULT_LOGIC.CONSULTATION.QBE.QBE_DIFFERENT)) valorOper = "<>";
                            argVo.setOperator(valorOper);
                            if (!argFormato.get(j).equals("")) {
                                if (argFormato.size() > 0) {
                                    formato = (String) argFormato.get(j);
                                    argVo.setFormat(formato);
                                }
                                if (argFormato.size() > 0 && argOrIsNull.size() > 0) {
                                    if (!argOrIsNull.get(j).equals("")) argVo.setOrIsNull("S"); else argVo.setOrIsNull("N");
                                }
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("###QBE: " + PlcConstants.DEFAULT_LOGIC.CONSULTATION.QBE.QBE_TYPE_ARGUMENT + " nomeAtributo: " + nomeFinalAtributo + " valor=" + valorAtributo + " oper=" + valorOper);
                            }
                            vo.add(argVo);
                        }
                    }
                    j++;
                }
            }
        } catch (Exception ex) {
            throw new PlcException("jcompany.erros.transfereBeansArg", new Object[] { ex }, ex, log);
        }
        return vo;
    }

    /**
	 * JCompany. Atribui os atributos da cl�usula order by ao ENTITY de argumentos
	 * 
	 * @param argVO
	 *            value object de argumentos
	 * @param orderBy
	 *            lista com os atributos de ordena��o
	 */
    public void montaOrderBy(List argVO, ArrayList orderBy, PlcActionMapping plcMapping) {
        log.debug("############# Entrou em montaOrderBy");
        if (orderBy == null) {
            log.debug("Entrou para colocar orderby declarativo");
            PlcArgEntity argVoOrderBy = new PlcArgEntity();
            argVoOrderBy.setType(PlcConstants.DEFAULT_LOGIC.CONSULTATION.QBE.QBE_TYPE_ORDER_BY);
            argVoOrderBy.setName(plcMapping.getOrderBySel());
            argVO.add(argVoOrderBy);
        } else {
            for (int i = 0; i < orderBy.size(); i++) {
                PlcArgEntity argVoOrderBy = new PlcArgEntity();
                argVoOrderBy.setType(PlcConstants.DEFAULT_LOGIC.CONSULTATION.QBE.QBE_TYPE_ORDER_BY);
                argVoOrderBy.setName(orderBy.get(i).toString());
                argVO.add(argVoOrderBy);
            }
        }
    }

    /**
	 * JCompany. Transfere as informa��es do form de entrada para um value
	 * object (vo) da classe de persist�ncia associada.
	 * 
	 * @param form
	 *            form de entrada de dados (deve ser do type DynaActionForm ou
	 *            DynaValidatorActionForm)
	 * @return um <code>Object</code> value object da classe de persist�ncia
	 */
    public Object[] montaVO(PlcBaseContextEntity context, PlcActionMapping plcMapping, DynaActionForm form, HttpServletRequest request) throws PlcException {
        log.debug("############# Entrou no montaVOPersistencia");
        PlcBaseEntity vo = null;
        Object chaveNatural = null;
        String modoGravacao = (String) form.get(PlcConstantsCommons.MODES.MODE);
        boolean eInclusao = modoGravacao.equals(PlcConstantsCommons.MODES.MODE_INSERTION) || (modoGravacao.equals("") && request.getSession().getAttribute(PlcConstantsCommons.ENTITY.PREFIX_OBJ + (String) config.get(plcMapping, PlcConstantsCommons.ENTITY_ROOT)) == null);
        if ("".equals((String) config.get(plcMapping, PlcConstantsCommons.ENTITY_ROOT))) return null;
        try {
            if (eInclusao) {
                log.debug("montaVOPersistencia: modo gravacao = inclusao ");
                vo = criaVOPrincipal(plcMapping, form);
            } else {
                log.debug("montaVOPersistencia: modo gravacao = alteracao ");
                if (request.getSession().getAttribute(PlcConstantsCommons.ENTITY.PREFIX_OBJ + config.get(plcMapping, PlcConstantsCommons.ENTITY_ROOT)) != null) vo = (PlcBaseEntity) PlcBeanCloneHelper.getInstance().cloneBean((Object) request.getSession().getAttribute(PlcConstantsCommons.ENTITY.PREFIX_OBJ + config.get(plcMapping, PlcConstantsCommons.ENTITY_ROOT)));
                if (vo == null) return null;
            }
            chaveNatural = transfereBeans(context, plcMapping, request, vo, form, "");
            if (vo == null) return null;
            boolean cardinalidadeOk = true;
            if (PlcActionMappingDet.class.isAssignableFrom(plcMapping.getClass()) && (config.get(plcMapping, PlcConstantsCommons.STEREOTYPE_COLLABORATION).toString().equals(PlcConstantsCommons.DEFAULT_LOGIC.DP.PATTERN_MASTER_DETAIL) || config.get(plcMapping, PlcConstantsCommons.STEREOTYPE_COLLABORATION).toString().equals(PlcConstantsCommons.DEFAULT_LOGIC.DP.PATTERN_MASTER_DETAIL_MANTAIN_DETAIL) || config.get(plcMapping, PlcConstantsCommons.STEREOTYPE_COLLABORATION).toString().equals(PlcConstantsCommons.DEFAULT_LOGIC.DP.PATTERN_MASTER_DETAIL_SUB_DETAIL))) cardinalidadeOk = montaVOComplementaDetalhes(context, plcMapping, request, form, vo, !eInclusao) != null;
            if (!cardinalidadeOk) return null;
        } catch (Exception e) {
            throw new PlcException("jcompany.erros.montaVOPersistencia", new Object[] { e }, e, log);
        }
        return new Object[] { vo, chaveNatural };
    }

    /**
	 * Criar uma instancia de um ou outro descendente, conforme o type selecionado no form-bean
	 * @return Instancia do descendente
	 */
    public PlcBaseEntity criaVOPrincipal(PlcActionMapping plcMapping, DynaActionForm form) throws PlcException {
        try {
            if (plcMapping.getDescendentes() == null) {
                Class c = ((Class) config.get(plcMapping, PlcConstantsCommons.ENTITY_ROOT_CLASS));
                if (null == c) {
                    String nomeClasse = (String) config.get(plcMapping, PlcConstantsCommons.ENTITY_ROOT);
                    c = Class.forName(nomeClasse);
                }
                return (PlcBaseEntity) c.newInstance();
            } else {
                if (form.get(PlcConstants.FORM.AUTOMATION.TYPE_PLC) == null) throw new PlcException("jcompany.erros.logica.ancestral.tipoPlc.sem.valor");
                Map<String, Class> descendentes = plcMapping.getDescendentes();
                for (Iterator iter = descendentes.values().iterator(); iter.hasNext(); ) {
                    Class c = (Class) iter.next();
                    DiscriminatorValue dv = (DiscriminatorValue) c.getAnnotation(DiscriminatorValue.class);
                    if (dv != null && form.getString(PlcConstants.FORM.AUTOMATION.TYPE_PLC).equals(dv.value())) return (PlcBaseEntity) c.newInstance();
                }
                throw new PlcException("jcompany.erros.logica.ancestral.tipoPlc.valor.invalido");
            }
        } catch (Exception e) {
            throw new PlcException("jcompany.error.generic", new Object[] { "montaVOCriaInstancia", e }, e, log);
        }
    }

    /**
	 * Devolve a classe principal, considerando heran�a com base em "tipoPlc" se estiver declarado
	 * @return Classe principal ou descendente em funcao da selecao
	 */
    public Class recuperaClassePrincipal(PlcActionMapping plcMapping, DynaActionForm f) throws PlcException {
        try {
            if (plcMapping.getDescendentes() == null) {
                Class c = (Class) config.get(plcMapping, PlcConstantsCommons.ENTITY_ROOT_CLASS);
                if (null == c) {
                    c = Class.forName((String) config.get(plcMapping, PlcConstantsCommons.ENTITY_ROOT));
                }
                return c;
            } else {
                if (f.get(PlcConstants.FORM.AUTOMATION.TYPE_PLC) == null) throw new PlcException("jcompany.erros.logica.ancestral.tipoPlc.sem.valor");
                Map<String, Class> descendentes = plcMapping.getDescendentes();
                for (Iterator iter = descendentes.values().iterator(); iter.hasNext(); ) {
                    Class c = (Class) iter.next();
                    DiscriminatorValue dv = (DiscriminatorValue) c.getAnnotation(DiscriminatorValue.class);
                    if (dv != null && f.getString(PlcConstants.FORM.AUTOMATION.TYPE_PLC).equals(dv.value())) return c;
                }
                throw new PlcException("jcompany.erros.logica.ancestral.tipoPlc.valor.invalido");
            }
        } catch (Exception e) {
            throw new PlcException("jcompany.error.generic", new Object[] { "recuperaClassePrincipal", e }, e, log);
        }
    }

    /**
	 * jCompany 3.0. Recebe um ArrayList de Detalhes (incluindo campos vazios) e
	 * monta uma refer�ncia ao objeto pai para aqueles itens preenchidos
	 * JCompany 1.5.3. Al�m disso, preenche campos de auditoria no ENTITY de
	 * detalhe, se for inclusao e auditoriaRigida = "N".
	 * 
	 * @param eAlteracao
	 */
    protected Object[] montaVOComplDetalhesMontaPai(PlcBaseContextEntity context, PlcActionMapping plcMapping, List listaOrigem, PlcBaseEntity voPai, String colDesprezar, String nomeClasseDetalhe, boolean zeroSignificativo, String testaDuplicata, HttpServletRequest request, boolean eAlteracao) throws Exception {
        List l = new ArrayList();
        int cont = 0;
        int linha = 0;
        if (listaOrigem != null) {
            Iterator i = listaOrigem.iterator();
            while (i.hasNext()) {
                PlcBaseEntity voDet = (PlcBaseEntity) i.next();
                Object ret = PropertyUtils.getProperty(voDet, colDesprezar);
                linha++;
                if (log.isDebugEnabled() && ret != null) log.debug("Valor da coluna a desprezar= " + ret);
                if (ret != null && (zeroSignificativo || !ret.equals("0")) && !ret.equals("")) {
                    if (((voDet.getIndExcPlc() == null) || (voDet.getIndExcPlc() != null && voDet.getIndExcPlc().equals("N"))) && !("exclui".equals(request.getAttribute(PlcConstantsCommons.WORKFLOW.IND_ORIGINAL_ACTION)))) {
                        if (plcMapping.getClassePrimaryKey() != null) {
                            PlcBaseEntity chaveDetalhe = voDet.getDynamicNaturalId();
                            if (!PropertyUtils.isReadable(chaveDetalhe, plcMapping.getClassePrimaryKey())) {
                                PlcEntityHelper voH = PlcEntityHelper.getInstance();
                                String propAgregada = voH.returnPropertyNameToClass(voPai.getClass().getName());
                                PropertyUtils.setProperty(chaveDetalhe, propAgregada, voPai);
                            } else if (!PropertyUtils.isReadable(voDet, plcMapping.getClassePrimaryKey())) {
                                PlcEntityHelper voH = PlcEntityHelper.getInstance();
                                String propAgregada = voH.returnPropertyNameToClass(voPai.getClass().getName());
                                PropertyUtils.setProperty(voDet, propAgregada, voPai);
                            } else {
                                transfereBeans(context, plcMapping, request, voDet, voPai.getDynamicNaturalId(), "");
                            }
                        } else {
                            if ("M:N".equals(plcMapping.getModalidade())) apiCompoeManyToMany(plcMapping, voDet, voPai); else apiCompoePaiNoDetalhe(plcMapping, voDet, voPai);
                        }
                        if ((config.get(plcMapping, PlcConstantsCommons.STEREOTYPE_COLLABORATION).toString()).startsWith("subdetalhe")) {
                            PlcActionMappingSubDet sDet = (PlcActionMappingSubDet) plcMapping;
                            Object zeroSignificativoSDet = config.get(sDet, PlcConstantsCommons.SUB_DETAIL_FLAG_DESC_ZERO);
                            Boolean resultado = false;
                            if (zeroSignificativoSDet != null) {
                                if (zeroSignificativoSDet.getClass().isAssignableFrom(String.class)) resultado = zeroSignificativoSDet != null && ((String) zeroSignificativoSDet).equals("0");
                                if (zeroSignificativoSDet.getClass().isAssignableFrom(Boolean.class)) resultado = (Boolean) zeroSignificativoSDet;
                            }
                            if (((String) config.get(sDet, PlcConstantsCommons.SUB_DETAIL_PARENT_ENTITY)).equals(voDet.getClass().getName())) {
                                retiraSubDetalhes((String) config.get(sDet, PlcConstantsCommons.SUB_DETAIL_FLAG_DESPISE), resultado.booleanValue(), (List) PropertyUtils.getProperty(voDet, (String) config.get(sDet, PlcConstantsCommons.SUB_DETAIL_PROP_NAME_COLLECTION)));
                                validaCardinalidadeSubDet(sDet, (List) PropertyUtils.getProperty(voDet, ((String) config.get(sDet, PlcConstantsCommons.SUB_DETAIL_PROP_NAME_COLLECTION))), linha);
                            }
                        }
                        if (testaDuplicata.equals("S")) getValidacaoService().validaDuplicatas(ret, l, colDesprezar, nomeClasseDetalhe, zeroSignificativo);
                        cont++;
                        if (!l.add(voDet)) {
                            throw new PlcException("jcompany.aplicacao.mestredetalhe.duplicidade." + nomeClasseDetalhe.toLowerCase(), null, log);
                        }
                    } else {
                        l.add(voDet);
                    }
                }
            }
        }
        if (log.isDebugEnabled()) log.debug("Vai retornar conjunto com " + l.size() + " elementos, podendo ter alguns a excluir.");
        return new Object[] { l, new Long(cont) };
    }

    /**
	 * 
	 * @param plcMapping
	 * @param voDet
	 * @param voPai
	 */
    protected void apiCompoePaiNoDetalhe(PlcActionMapping plcMapping, PlcBaseEntity voDet, PlcBaseEntity voPai) throws PlcException {
        log.debug("############### Entrou em apiCompoePaiNoDetalhe");
        try {
            String sufixoPadraoEntidade = PlcConfigHelper.getInstance().get(PlcConfigSuffixClass.class).entitySuffix();
            String voProp = (String) PlcConfigStrutsHelper.getInstance().get(plcMapping, PlcConstantsCommons.ENTITY_ROOT_PROP);
            if (PropertyUtils.isReadable(voDet, voProp)) PropertyUtils.setProperty(voDet, voProp, voPai); else if (PropertyUtils.isReadable(voDet, voProp + sufixoPadraoEntidade)) PropertyUtils.setProperty(voDet, voProp + sufixoPadraoEntidade, voPai); else if ((PropertyUtils.isReadable(voDet, ENTITY.PARENT_PLC))) PropertyUtils.setProperty(voDet, ENTITY.PARENT_PLC, voPai); else throw new PlcException("jcompany.erro.nao.achou.prop.padrao.pai", new Object[] { voPai.getClass().getSimpleName(), voDet.getClass().getSimpleName(), voProp + ", " + voProp + "ENTITY, " + ENTITY.PARENT_PLC });
        } catch (Exception e) {
            throw new PlcException("jcompany.error.generic", new Object[] { "apiCompoePaiNoDetalhe", e }, e, log);
        }
    }

    /**
	 * Monta referencias do pai no detalhe em relacionamentos M:N (ManyToMany), tratando cole��es como List ou Set.
	 */
    protected void apiCompoeManyToMany(PlcActionMapping plcMapping, PlcBaseEntity voDet, PlcBaseEntity voPai) throws PlcException {
        log.debug("############### Entrou em apiCompoePaiNoDetalhe");
        try {
            String sufixoPadraoEntidade = PlcConfigHelper.getInstance().get(PlcConfigSuffixClass.class).entitySuffix();
            String propPai = plcMapping.getValueObjectProp();
            if (!PropertyUtils.isReadable(voDet, plcMapping.getValueObjectProp())) {
                if (PropertyUtils.isReadable(voDet, plcMapping.getValueObjectProp() + sufixoPadraoEntidade)) propPai = plcMapping.getValueObjectProp() + sufixoPadraoEntidade; else throw new PlcException("jcompany.erro.nao.achou.prop.padrao.pai", new Object[] { voPai.getClass().getSimpleName(), voDet.getClass().getSimpleName(), plcMapping.getValueObjectProp() + ", " + plcMapping.getValueObjectProp() + sufixoPadraoEntidade });
            }
            if (!("S".equals(voDet.getIndExcPlc()))) {
                if (Set.class.isAssignableFrom(PropertyUtils.getPropertyType(voDet, propPai))) {
                    Set s = (Set) PropertyUtils.getProperty(voDet, plcMapping.getValueObjectProp());
                    if (s == null) s = new HashSet();
                    s.add(voPai);
                } else if (List.class.isAssignableFrom(PropertyUtils.getPropertyType(voDet, propPai))) {
                    List l = (List) PropertyUtils.getProperty(voDet, plcMapping.getValueObjectProp());
                    if (l == null) l = new ArrayList();
                    l.add(voPai);
                } else throw new PlcException("jcompany.erro.mapeamento.manytomany", new Object[] { voPai.getClass().getSimpleName(), voDet.getClass().getSimpleName(), plcMapping.getValueObjectProp() });
            }
        } catch (Exception e) {
            throw new PlcException("jcompany.error.generic", new Object[] { "apiCompoeManyToMany", e }, e, log);
        }
    }

    /**
	 * jCompany 2.5 Valida cardinalidade de Mestre-Detalhe-SubDetalhe
	 */
    private void validaCardinalidadeSubDet(PlcActionMappingSubDet sDet, List subDetalhePlc, int linha) throws PlcException {
        log.debug("######## Entrou em validaCardinalidadeSubDet");
        String subDetalheCardinalidade = sDet.getSubDetalheCardinalidade();
        String nomeClasse = sDet.getSubDetalheVO();
        int total = verificaTotValidos(subDetalhePlc, (String) config.get(sDet, PlcConstantsCommons.SUB_DETAIL_FLAG_DESPISE), sDet.getSubDetalheFlagDescZero());
        int separador = subDetalheCardinalidade.indexOf("..");
        int minimo = new Long(subDetalheCardinalidade.substring(0, separador)).intValue();
        String maximoS = subDetalheCardinalidade.substring(separador + 2, subDetalheCardinalidade.length());
        int maximo = 0;
        if (log.isDebugEnabled()) log.debug("VAI COMPARAR total de registros " + total + " com minimo = " + minimo + " e maximo = " + maximo);
        if (maximoS.equals("*") || maximoS.toLowerCase().equals("n")) maximo = 9999; else maximo = new Long(maximoS).intValue();
        if (total > maximo) {
            String nomeErro = nomeClasse;
            if (nomeClasse.indexOf(".") > -1) nomeErro = nomeClasse.substring(nomeClasse.lastIndexOf(".") + 1, nomeClasse.length());
            throw new PlcException("jcompany.aplicacao.mestredetalhesub.cardinalidade.max." + nomeErro.toLowerCase(), new Object[] { new Long(linha) + "" });
        }
        if (total < minimo) {
            String nomeErro = nomeClasse;
            if (nomeClasse.indexOf(".") > -1) nomeErro = nomeClasse.substring(nomeClasse.lastIndexOf(".") + 1, nomeClasse.length());
            throw new PlcException("jcompany.aplicacao.mestredetalhesub.cardinalidade.min." + nomeErro.toLowerCase(), new Object[] { new Long(linha) + "" });
        }
    }

    /**
	 * jCompany 2.5. Contabiliza numero de sub-detalhes v�lidos para valida��o
	 * 
	 * @param subDetalhePlc
	 *            Cole��o de VOs
	 * @param subDetalheFlagDesprezar
	 *            nome da coluna flag (se n�o informada despreza a linha
	 * @param subDetalheFlagDescZero
	 *            "0" ou null
	 * @return total de linhas validas, considerando-se a coluna flag e valor
	 */
    private int verificaTotValidos(List subDetalhePlc, String subDetalheFlagDesprezar, String subDetalheFlagDescZero) throws PlcException {
        log.debug("######## Entrou em verificaTotValidos");
        int cont = 0;
        if (subDetalhePlc == null) return 0;
        Iterator i = subDetalhePlc.iterator();
        while (i.hasNext()) {
            PlcBaseEntity vo = (PlcBaseEntity) i.next();
            if ((vo.getIndExcPlc() == null || !vo.getIndExcPlc().equals("S"))) {
                String obj = (String) PlcReflectionHelper.getInstance().callGetter(vo, subDetalheFlagDesprezar);
                if (!(obj == null || (obj != null && obj.equals("")) || (obj != null && obj.equals("0") && subDetalheFlagDescZero.equals("0")))) cont++;
            }
        }
        return cont;
    }

    /**
	 * @param subDetalheFlagDesprezar
	 * @param subDetalhePlc
	 */
    private void retiraSubDetalhes(String subDetalheFlagDesprezar, boolean zeroSignificativo, List subDetalhePlc) throws PlcException {
        log.debug("######## Entrou em retiraSubDetalhes");
        if (subDetalhePlc != null) {
            Iterator i = subDetalhePlc.iterator();
            while (i.hasNext()) {
                PlcBaseEntity sDet = (PlcBaseEntity) i.next();
                Object ret = PlcReflectionHelper.getInstance().callGetter(sDet, subDetalheFlagDesprezar);
                if (log.isDebugEnabled() && ret != null) log.debug("Valor da coluna a desprezar do subDetalhe= " + ret);
                if (ret != null && (zeroSignificativo || !ret.equals("0")) && !ret.equals("")) {
                    log.debug("informou ok");
                } else i.remove();
            }
        }
    }

    /**
	 * jCompany. Retira todos os registros de detalhe que possuam a marca de
	 * exclus�o do form, para refletir a exclus�o.
	 */
    public void removeDetalhesMarcados(PlcActionMapping plcMapping, DynaActionForm f) throws PlcException {
        PlcActionMappingDet plcMappingDet = (PlcActionMappingDet) plcMapping;
        try {
            PlcBaseEntity voAux;
            List nomeProps = (List) config.get(plcMappingDet, "detNomePropriedade");
            List nomeClasses = (List) config.get(plcMappingDet, "detVO");
            Iterator i = nomeProps.iterator();
            String nomeProp = "";
            while (i.hasNext()) {
                nomeProp = (String) i.next();
                log.debug("Vai pegar detalhe do form");
                List listaOrigem = (List) f.get(nomeProp);
                if (listaOrigem == null) return;
                List listaAux = new ArrayList();
                if (log.isDebugEnabled()) log.debug("Pegou lista com " + listaOrigem.size() + " elementos");
                Iterator j = listaOrigem.iterator();
                while (j.hasNext()) {
                    voAux = (PlcBaseEntity) j.next();
                    if (voAux.getIndExcPlc() != null && voAux.getIndExcPlc().equals("S")) listaAux.add(voAux);
                }
                int tam = listaOrigem.size();
                int tamExc = listaAux.size();
                listaOrigem.removeAll(listaAux);
                if (tam - tamExc < listaOrigem.size()) {
                    PlcEntityHelper vo = PlcEntityHelper.getInstance();
                    vo.excludeMarked(listaOrigem);
                }
                if (listaAux.size() > 0) f.set(nomeProp, listaOrigem);
            }
        } catch (Exception e) {
            throw new PlcException("jcompany.erros.remove.detalhes", new Object[] { e }, e, log);
        }
    }

    /**
	 * @param flagDesprezar
	 * @param plcVO
	 */
    public boolean desprezaItem(String flagDesprezar, PlcBaseEntity plcVO, String classePK) throws PlcException {
        try {
            if (flagDesprezar == null || flagDesprezar.equals("")) return false;
            boolean propChaveNatural = false;
            if (StringUtils.isBlank(classePK)) {
                PlcPrimaryKey chavePrimaria = plcVO.getClass().getAnnotation(PlcPrimaryKey.class);
                if (chavePrimaria != null) {
                    String[] propriedades = chavePrimaria.properties();
                    for (String prop : propriedades) {
                        if (flagDesprezar.equals(prop)) {
                            propChaveNatural = true;
                            break;
                        }
                    }
                }
            }
            if (!propChaveNatural) {
                Object val = PropertyUtils.getNestedProperty(plcVO, flagDesprezar);
                if (val != null && !val.toString().trim().equals("")) return false; else return true;
            } else {
                Object idNatural = PropertyUtils.getNestedProperty(plcVO, "idNatural");
                Object val = PropertyUtils.getNestedProperty(idNatural, flagDesprezar);
                if (val != null && !((String) val).trim().equals("")) return false; else return true;
            }
        } catch (Exception e) {
            throw new PlcException("jcompany.error.generic", new Object[] { "desprezaItem", e }, e, log);
        }
    }

    /**
	 * Complementa ENTITY com informa��es das colunas de cabe�alho
	 * 
	 * @return Vo com complementa��o
	 */
    public PlcBaseEntity complementaVOCRUDTabular(PlcActionMapping plcMapping, HttpServletRequest request, ActionForm form, PlcBaseEntity plcAux) throws Exception {
        List colNome = plcMapping.getColCabecalhoNome();
        List colTipo = plcMapping.getColCabecalhoTipo();
        List colPara = plcMapping.getColCabecalhoPara();
        DynaActionForm f = (DynaActionForm) form;
        Iterator i = colNome.iterator();
        Iterator j = colTipo.iterator();
        Iterator k = colPara.iterator();
        String nomeCol = "";
        String tipoCol = "";
        String valor = "";
        String paraCol = "";
        while (i.hasNext()) {
            nomeCol = (String) i.next();
            tipoCol = (String) j.next();
            paraCol = (String) k.next();
            valor = f.get(nomeCol) + "";
            if (!valor.equals("")) {
                if (tipoCol.toLowerCase().equals("long")) {
                    BeanUtils.copyProperty(plcAux, paraCol, new Long(valor));
                } else if (tipoCol.toLowerCase().equals("date")) {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    Date data = formatter.parse(valor);
                    BeanUtils.copyProperty(plcAux, paraCol, data);
                } else BeanUtils.copyProperty(plcAux, paraCol, valor);
            }
        }
        return plcAux;
    }

    /**
	 * JCompany. Instancia uma Classe que seja identificada com OID
	 */
    public Object _criaClasseAgregada(DynaActionForm form, PropertyDescriptor pd, Object valorInformadoId, String nomePropForm) throws PlcException {
        if (log.isDebugEnabled()) log.debug("################# Entrou criar classe agregada para " + pd.getPropertyType() + " com OID=" + valorInformadoId);
        Class classe = pd.getPropertyType();
        PlcBaseEntity objClasse = null;
        try {
            PlcAggregatePropLocator agregadoPropLocator = PlcAggregatePropLocator.getInstance();
            objClasse = agregadoPropLocator.getAggregatedClassObject(classe);
            PlcPrimaryKey chavePrimaria;
            try {
                chavePrimaria = objClasse.getClass().getAnnotation(PlcPrimaryKey.class);
            } catch (Exception e) {
                chavePrimaria = null;
            }
            if (chavePrimaria == null) {
                if (StringUtils.isNotBlank(nomePropForm)) {
                    String valorProp = (String) form.get(nomePropForm);
                    if (StringUtils.isBlank(valorProp)) {
                        objClasse = null;
                    }
                }
                if (valorInformadoId != null && !((String) valorInformadoId).equals("0") && !((String) valorInformadoId).equals("")) {
                    if (log.isDebugEnabled()) log.debug("Classe = " + classe.getName() + " valorInformadoId = " + valorInformadoId.toString());
                    objClasse.setIdAux((String) valorInformadoId);
                }
            } else {
                String[] propriedades = chavePrimaria.properties();
                PlcBaseEntity objetoKeyVO = (PlcBaseEntity) chavePrimaria.classe().newInstance();
                for (String prop : propriedades) {
                    String valorProp = (String) form.get(pd.getName() + "_" + prop);
                    Class type = PropertyUtils.getPropertyType(objetoKeyVO, prop);
                    if (StringUtils.isNotBlank(valorProp)) {
                        if (Long.class.isAssignableFrom(type)) {
                            PropertyUtils.setProperty(objetoKeyVO, prop, Long.valueOf(valorProp));
                        } else {
                            PropertyUtils.setProperty(objetoKeyVO, prop, valorProp);
                        }
                    } else {
                        objetoKeyVO = null;
                        objClasse = null;
                        break;
                    }
                }
                if (objClasse != null) objClasse.setIdNatural(objetoKeyVO);
            }
        } catch (Exception e) {
            throw new PlcException("jcompany.erros.criaclasseagregada", null, e, log);
        }
        return objClasse;
    }

    /**
	 * jCompany 3.1. Evita de receber request.
	 */
    public Object criaClasseAgregada(DynaActionForm form, PropertyDescriptor pd, Object valorInformadoId, String nomePropForm) throws PlcException {
        return _criaClasseAgregada(form, pd, valorInformadoId, nomePropForm);
    }

    /**
	 * JCompany. M�todo que limpa campos de detalhe dos forms, para evitar
	 * problemas no caso da clonagem, por exemplo.
	 */
    public void limparDetalhes(PlcActionMapping plcMapping, DynaActionForm f) throws PlcException {
        log.debug("############## Entrou para limpar detalhes");
        try {
            if (config.get(plcMapping, PlcConstantsCommons.STEREOTYPE_COLLABORATION).toString().equals(PlcConstantsCommons.DEFAULT_LOGIC.DP.PATTERN_MASTER_DETAIL) || config.get(plcMapping, PlcConstantsCommons.STEREOTYPE_COLLABORATION).toString().equals(PlcConstantsCommons.DEFAULT_LOGIC.DP.PATTERN_MASTER_DETAIL_MANTAIN_DETAIL) || config.get(plcMapping, PlcConstantsCommons.STEREOTYPE_COLLABORATION).toString().equals(PlcConstantsCommons.DEFAULT_LOGIC.DP.PATTERN_MASTER_DETAIL_SUB_DETAIL)) {
                log.debug("Vai limpar detalhes");
                PlcActionMappingDet plcMappingDet = (PlcActionMappingDet) plcMapping;
                List auxs = (List) config.get(plcMappingDet, "detNomePropriedade");
                Iterator i = auxs.iterator();
                String nomeProp = "";
                ArrayList l = new ArrayList();
                while (i.hasNext()) {
                    nomeProp = (String) i.next();
                    if (log.isDebugEnabled()) log.debug("Vai limpar detalhes para " + nomeProp);
                    f.set(nomeProp, l);
                }
            }
        } catch (Exception e) {
            log.fatal("Erro ao tentar limpar detalhes =" + e);
            throw new PlcException("jcompany.erros.inclui", new Object[] { e }, e, log);
        }
    }

    /**
	 * @since jCompany 1.5. M�todo que limpa as referencias do mestre no detalhe e chaves
	 * prim�rias do detalhe, para permitir clonagem.
	 * @since jCompany 3.2. Evita remover OID na clonagem quando o detalhe for de l�gica ManyToMany 
	 */
    public void clonarDetalhes(PlcActionMapping plcMapping, DynaActionForm f) throws PlcException {
        try {
            if (config.get(plcMapping, PlcConstantsCommons.STEREOTYPE_COLLABORATION).toString().equals(PlcConstantsCommons.DEFAULT_LOGIC.DP.PATTERN_MASTER_DETAIL) || config.get(plcMapping, PlcConstantsCommons.STEREOTYPE_COLLABORATION).toString().equals(PlcConstantsCommons.DEFAULT_LOGIC.DP.PATTERN_MASTER_DETAIL_MANTAIN_DETAIL) || config.get(plcMapping, PlcConstantsCommons.STEREOTYPE_COLLABORATION).toString().equals(PlcConstantsCommons.DEFAULT_LOGIC.DP.PATTERN_MASTER_DETAIL_SUB_DETAIL)) {
                PlcActionMappingDet plcMappingDet = (PlcActionMappingDet) plcMapping;
                List auxs = (List) config.get(plcMappingDet, "detNomePropriedade");
                Iterator i = auxs.iterator();
                String nomeProp = "";
                while (i.hasNext()) {
                    nomeProp = (String) i.next();
                    if (log.isDebugEnabled()) log.debug("Vai clonar detalhes para " + nomeProp);
                    List l = (List) f.get(nomeProp);
                    Long lnulo = null;
                    Iterator j = l.iterator();
                    while (j.hasNext()) {
                        PlcBaseEntity vo = (PlcBaseEntity) j.next();
                        if (!PlcAnnotationHelper.getInstance().existsAnnotationManyToMany(vo.getClass())) vo.setId(lnulo);
                    }
                }
                if (config.get(plcMapping, PlcConstantsCommons.STEREOTYPE_COLLABORATION).toString().equals(PlcConstantsCommons.DEFAULT_LOGIC.DP.PATTERN_MASTER_DETAIL_SUB_DETAIL)) clonarSubDetalhes(f, (PlcActionMappingSubDet) plcMapping);
            }
        } catch (Exception e) {
            log.fatal("Erro ao tentar limpar detalhes =" + e);
            throw new PlcException("jcompany.erros.inclui", new Object[] { e }, e, log);
        }
    }

    /**
	 * jCompany 2.5.3. Limpa referencias bilaterais de sub-detalhes durante o
	 * processo de clonagem
	 */
    private void clonarSubDetalhes(DynaActionForm f, PlcActionMappingSubDet plcMapping) throws PlcException {
        log.debug("######## Entrou em verificaClonaSubDetalhes");
        try {
            String nomeVODetPai = plcMapping.getSubDetalhePaiVO();
            Iterator i = ((List) config.get(plcMapping, "detVO")).iterator();
            int cont = 0;
            while (i.hasNext()) {
                String nomeDetalhe = (String) i.next();
                if (nomeDetalhe.equalsIgnoreCase(nomeVODetPai)) {
                    String nomePropDet = (String) ((List) config.get(plcMapping, "detNomePropriedade")).get(cont);
                    List l = (List) f.get(nomePropDet);
                    if (l != null && l.size() > 0) {
                        Iterator j = l.iterator();
                        while (j.hasNext()) {
                            PlcBaseEntity voDet = (PlcBaseEntity) j.next();
                            List subDetCol = null;
                            if (PropertyUtils.getProperty(voDet, plcMapping.getSubDetalhePropNomeColecao()) != null) {
                                if (((List) PropertyUtils.getProperty(voDet, plcMapping.getSubDetalhePropNomeColecao())).size() > 0) subDetCol = new ArrayList();
                                Iterator k = ((List) PropertyUtils.getProperty(voDet, plcMapping.getSubDetalhePropNomeColecao())).iterator();
                                while (k.hasNext()) {
                                    try {
                                        PlcBaseEntity voSubDet = (PlcBaseEntity) k.next();
                                        PlcBaseEntity voSubDetClone = (PlcBaseEntity) BeanUtils.cloneBean(voSubDet);
                                        apiCompoePaiNoDetalhe(plcMapping, voDet, null);
                                        voSubDetClone.setId(null);
                                        subDetCol.add(voSubDetClone);
                                    } catch (Exception e) {
                                        throw new PlcException("jcompany.error.generic", new Object[] { "verificaClonaSubDetalhes", e }, e, log);
                                    }
                                }
                            }
                            PropertyUtils.setProperty(voDet, plcMapping.getSubDetalhePropNomeColecao(), subDetCol);
                        }
                    }
                }
                cont++;
            }
        } catch (Exception e) {
            throw new PlcException("jcompany.error.generic", new Object[] { "clonarSubDetalhes", e }, e, log);
        }
    }

    /**
	 * jCompany. Devolve o Value Object dispon�vel na sess�o, para l�gicas de
	 * recupera��o de dados.
	 */
    public PlcBaseEntity getVOCorrente(PlcActionMapping plcMapping, HttpServletRequest request) throws PlcException {
        PlcBaseEntity plcVO = (PlcBaseEntity) request.getSession().getAttribute(PlcConstantsCommons.ENTITY.PREFIX_OBJ + config.get(plcMapping, PlcConstantsCommons.ENTITY_ROOT));
        return plcVO;
    }

    /**
	 * Initializa form-bean, fazendo registros iniciais para detalhes
	 * "porDemanda", no campo detalhePorDemandaPlc, incluindo nomes de detalhes
	 * (propriedades) separados por virgula, na forma:<br>
	 * detalhePorDemandaPlc=detalhe2,detalhe4
	 * 
	 * Se for l�gica de heran�a, preserva o 
	 * 
	 * @since jCompany 3.1
	 * @param f
	 *            Form-bean a ser inicializado
	 */
    public void initializaForm(PlcActionMapping plcMapping, DynaActionForm f) throws PlcException {
        String valorDiscriminatorInicial = null;
        if (plcMapping.getDiscriminadorInicial() != null && !plcMapping.getDiscriminadorInicial().equals("")) {
            valorDiscriminatorInicial = f.getString("tipoPlc");
        }
        f.initialize(plcMapping);
        if (valorDiscriminatorInicial != null && !valorDiscriminatorInicial.equals("")) {
            f.set("tipoPlc", valorDiscriminatorInicial);
        }
        if (PlcActionMappingDet.class.isAssignableFrom(plcMapping.getClass())) {
            PlcActionMappingDet actionMappingDet = (PlcActionMappingDet) plcMapping;
            if (config.get(plcMapping, "detailsOnDemand") != null) {
                Iterator i = ((Map<String, Class>) config.get(plcMapping, "detailsOnDemand")).keySet().iterator();
                StringBuffer s = new StringBuffer("");
                int cont = 0;
                while (i.hasNext()) {
                    String detalhePorDemanda = (String) i.next();
                    if (cont > 0) {
                        s.append(",");
                    }
                    s.append(detalhePorDemanda);
                    cont++;
                }
                if (log.isDebugEnabled()) log.debug("Vai montar lista de detalhes por demanda com " + s.toString());
                f.set(PlcConstants.FORM.AUTOMATION.DETAIL.DETAIL_ON_DEMAND, s.toString());
            }
        }
    }

    /**
	 * Retira VOs com itens de exclusao marcados da cole��o
	 * 
	 * @param l
	 *            Cole��o de VOs descendentes de PlcBaseEntity
	 * @return Cole��o com VOs exclu�dos removidos
	 */
    public ArrayList retiraExcluidos(List l) {
        log.debug("############## Entrou para retirar excluidos da cole��o");
        Iterator i = l.iterator();
        ArrayList lAux = new ArrayList();
        while (i.hasNext()) {
            PlcBaseEntity vo = (PlcBaseEntity) i.next();
            if (vo.getIndExcPlc() != null && vo.getIndExcPlc().toLowerCase().equals("s")) {
                if (log.isDebugEnabled()) log.debug("Vai remover item com id=" + vo.getId());
                i.remove();
            }
        }
        lAux.addAll(l);
        return lAux;
    }

    /**
	 * Adaptador para manter compatibilidade. M�todo deve receber indicador se
	 * clona detalhes.
	 */
    public PlcBaseEntity montaVOAnterior(String modoGravacao, HttpServletRequest request, String tipoVO) throws PlcException {
        try {
            PlcBaseEntity voAnt = null;
            if (!modoGravacao.equals(PlcConstantsCommons.MODES.MODE_INSERTION) && request.getSession().getAttribute(PlcConstantsCommons.ENTITY.PREFIX_OBJ + tipoVO) != null) {
                PlcBaseEntity voSessao = (PlcBaseEntity) request.getSession().getAttribute(PlcConstantsCommons.ENTITY.PREFIX_OBJ + tipoVO);
                voAnt = (PlcBaseEntity) PlcBeanCloneHelper.getInstance().cloneBean(voSessao);
            }
            return voAnt;
        } catch (Exception e) {
            throw new PlcException("jcompany.error.generic", new Object[] { "montaVOAnterior", e }, e, log);
        }
    }

    /**
	 * Recupera a lista da sess�o relacionada ao type do objeto Utilizado para
	 * recupera��o de lista anterior de l�gica tabular e crud-tabular
	 */
    public List recuperaListaAnteriorSessao(String modoGravacao, HttpServletRequest request, String tipoVO, PlcActionMapping plcMapping) throws PlcException {
        log.debug("######## Entrou em montaListaAnterior");
        List listaAnterior = null;
        if ((config.get(plcMapping, PlcConstantsCommons.STEREOTYPE_COLLABORATION).toString().equals(PlcConstantsCommons.DEFAULT_LOGIC.DP.PATTERN_TABULAR) || config.get(plcMapping, PlcConstantsCommons.STEREOTYPE_COLLABORATION).toString().equals(PlcConstantsCommons.DEFAULT_LOGIC.DP.PATTERN_CRUD_TABULAR) || !modoGravacao.equals(PlcConstantsCommons.MODES.MODE_INSERTION)) && request.getSession().getAttribute(PlcConstantsCommons.ENTITY.PREFIX_OBJ + tipoVO + PlcConstants.SUFFIX_LIST) != null) {
            listaAnterior = (List) request.getSession().getAttribute(PlcConstantsCommons.ENTITY.PREFIX_OBJ + tipoVO + PlcConstants.SUFFIX_LIST);
            log.debug("Tamanho lista Anterior=" + listaAnterior.size());
        }
        return listaAnterior;
    }

    /**
	 * jCompany 2.2 Adiciona registros em branco de sub-detalhes em uma cole��o
	 * de detalhes.
	 * 
	 * @param nomePropColecao
	 *            Nome da propriedade que cont�m a cole��o de sub-detalhes
	 */
    public void criaNovoSubDetalhe(PlcBaseEntity plcDetVO, String numAux, String classeSub, String nomePropColecao) throws Exception {
        log.debug("######## Entrou em criaNovoSubDetalhe");
        if (PropertyUtils.getProperty(plcDetVO, nomePropColecao) == null) PropertyUtils.setProperty(plcDetVO, nomePropColecao, new ArrayList());
        int num = new Long(numAux).intValue();
        for (int i = 1; i <= num; i++) {
            PlcBaseEntity plcObj = (PlcBaseEntity) Class.forName(classeSub).newInstance();
            ((List) PropertyUtils.getProperty(plcDetVO, nomePropColecao)).add(plcObj);
            PropertyUtils.setProperty(plcObj, plcDetVO.getPropertyNamePlc(), plcDetVO);
        }
    }

    /**
	 * jCompany 2.5 Recupera cole��o de detalhes pai do subdetalhe corrente.
	 * 
	 * @return Cole��o de detalhes pai do subdetalhe ou null se n�o encontrou
	 */
    public List recuperaDetalhePai(PlcActionMappingSubDet plcMapping, DynaValidatorActionForm f) throws PlcException {
        log.debug("######## Entrou em recuperaDetalhePai");
        try {
            String subDetalhePai = (String) config.get(plcMapping, PlcConstantsCommons.SUB_DETAIL_PARENT_ENTITY);
            Iterator i = ((List) config.get(plcMapping, "detVO")).iterator();
            int pos = 0;
            while (i.hasNext()) {
                String nomeDetalhe = (String) i.next();
                if (nomeDetalhe.equals(subDetalhePai)) {
                    String nome = ((List) config.get(plcMapping, "detNomePropriedade")).get(pos) + "";
                    if (log.isDebugEnabled()) log.debug("vai retornar cole��o em " + nome);
                    return (List) f.get(nome);
                }
                pos++;
            }
        } catch (Exception e) {
            throw new PlcException("jcompany.error.generic", new Object[] { "recuperaDetalhePai", e }, e, log);
        }
        return null;
    }

    /**
	 * jCompany 2.5.1. Lembra detalhes na sess�o, no aposEditar ou aposGravar,
	 * caso o desenvolvedor tenha marcado na logic detailRemember=S
	 */
    public void lembraDetalhesSessao(PlcActionMappingDet plcMappingDet, DynaActionForm form, HttpServletRequest request, HttpServletResponse response, PlcBaseEntity voAtual) throws PlcException {
        log.debug("##### Entrando para guardar detalhes da sessao");
        String tipoVO = (String) config.get(plcMappingDet, PlcConstantsCommons.ENTITY_ROOT);
        Collection novoSetDetalhe = null;
        String nomeProp = "";
        List nomeDets = (List) config.get(plcMappingDet, "detNomePropriedade");
        Iterator i = nomeDets.iterator();
        while (i.hasNext()) {
            nomeProp = (String) i.next();
            novoSetDetalhe = null;
            PlcBaseEntity novoVODetalhe = null;
            int pos = nomeProp.indexOf("_Det");
            if (pos >= 0) nomeProp = StringUtils.substring(nomeProp, 0, pos);
            try {
                Class tipoPropriedade = PropertyUtils.getPropertyType(voAtual, nomeProp);
                if (Set.class.isAssignableFrom(tipoPropriedade)) {
                    novoSetDetalhe = new HashSet();
                } else {
                    novoSetDetalhe = new ArrayList();
                }
                Collection detalhes = (Collection) PropertyUtils.getSimpleProperty(voAtual, nomeProp);
                if (detalhes != null) {
                    Iterator j = detalhes.iterator();
                    while (j.hasNext()) {
                        novoVODetalhe = new PlcBaseEntity();
                        if (log.isDebugEnabled()) log.debug("detalhe " + novoVODetalhe);
                        PlcBaseEntity voDetalhe = (PlcBaseEntity) j.next();
                        novoVODetalhe = (PlcBaseEntity) BeanUtils.cloneBean(voDetalhe);
                        if (PlcActionMappingSubDet.class.isAssignableFrom(plcMappingDet.getClass())) {
                            if (PropertyUtils.isReadable(novoVODetalhe, ((PlcActionMappingSubDet) plcMappingDet).getSubDetalhePropNomeColecao()) && PropertyUtils.getProperty(novoVODetalhe, ((PlcActionMappingSubDet) plcMappingDet).getSubDetalhePropNomeColecao()) != null) {
                                lembraSubDetalhesSessao((PlcActionMappingSubDet) plcMappingDet, form, request, response, novoVODetalhe);
                            }
                        }
                        novoSetDetalhe.add(novoVODetalhe);
                    }
                    PropertyUtils.setSimpleProperty(voAtual, nomeProp, novoSetDetalhe);
                }
            } catch (Exception e) {
                throw new PlcException("jcompany.error.generic", new Object[] { "lembraDetalhesSessao", e }, e, log);
            }
            request.getSession().setAttribute(PlcConstantsCommons.ENTITY.PREFIX_OBJ + tipoVO, voAtual);
        }
    }

    public void lembraSubDetalhesSessao(PlcActionMappingSubDet plcMappingSubDet, DynaActionForm form, HttpServletRequest request, HttpServletResponse response, PlcBaseEntity voDetalhe) throws Exception {
        log.debug("##### Entrando para guardar sub-detalhes da sessao");
        List novoListSubDetalhe = new ArrayList();
        PlcBaseEntity novoVOSubDetalhe = null;
        PlcBaseEntity voSubDetalhe = null;
        List subDetalhes = (List) PropertyUtils.getProperty(voDetalhe, plcMappingSubDet.getSubDetalhePropNomeColecao());
        Iterator k = subDetalhes.iterator();
        while (k.hasNext()) {
            voSubDetalhe = (PlcBaseEntity) k.next();
            if (log.isDebugEnabled()) log.debug("subdetalhe " + voSubDetalhe);
            novoVOSubDetalhe = new PlcBaseEntity();
            novoVOSubDetalhe = (PlcBaseEntity) BeanUtils.cloneBean(voSubDetalhe);
            novoListSubDetalhe.add(novoVOSubDetalhe);
        }
        PropertyUtils.setProperty(voDetalhe, plcMappingSubDet.getSubDetalhePropNomeColecao(), novoListSubDetalhe);
    }

    /**
	 * jCompany 2.5.1. Lembra tabular na sess�o, no aposEditar ou aposGravar,
	 * caso o usu�rio tenha marcado na logic detailRemember=S
	 */
    public void lembraTabularSessao(PlcActionMapping plcMapping, DynaActionForm form, HttpServletRequest request, HttpServletResponse response, List lista) throws PlcException {
        log.debug("##### Entrando para guardar tabular da sessao");
        String tipoVO = (String) config.get(plcMapping, PlcConstantsCommons.ENTITY_ROOT);
        List listaNova = new ArrayList();
        if (lista == null) {
            log.debug("lista tabular nula.");
            return;
        }
        try {
            Class classeVO = Class.forName(tipoVO);
            PlcPrimaryKey chavePrimaria = (PlcPrimaryKey) classeVO.getAnnotation(PlcPrimaryKey.class);
            for (Iterator iter = lista.iterator(); iter.hasNext(); ) {
                PlcBaseEntity objAtual = (PlcBaseEntity) iter.next();
                PlcBaseEntity novoObj = (PlcBaseEntity) BeanUtils.cloneBean(objAtual);
                if (chavePrimaria != null) {
                    Object idNatural = BeanUtils.cloneBean(PropertyUtils.getProperty(objAtual, "idNatural"));
                    PropertyUtils.setProperty(novoObj, "idNatural", idNatural);
                }
                listaNova.add(novoObj);
            }
            request.getSession().setAttribute(PlcConstantsCommons.ENTITY.PREFIX_OBJ + tipoVO + PlcConstants.SUFFIX_LIST, listaNova);
        } catch (Exception e) {
            throw new PlcException(e.getMessage(), e, log);
        }
    }

    /**
	 * jCompany 2.7.3 Monta propriedades do grafo do ENTITY para form-bean, a partir
	 * da sintaxe:
	 * <p>
	 * [propriedade no form-bean]=[propriedade no grafo do ENTITY]
	 * 
	 * @param listaMontagem
	 *            Cole��o com todas as montagens declaradas
	 * @param vo
	 *            Grafo do Vo para pegar origem do dado
	 */
    public void montaPropsVOParaFormBean(List listaMontagem, DynaActionForm f, PlcBaseEntity vo) throws PlcException {
        log.debug("######## Entrou em montaPropsVOParaFormBean");
        try {
            Iterator i = listaMontagem.iterator();
            while (i.hasNext()) {
                String regraMontagem = (String) i.next();
                String campo = regraMontagem.substring(0, regraMontagem.indexOf("="));
                String grafo = regraMontagem.substring(regraMontagem.indexOf("=valueObject.") + 13);
                Object valor = null;
                try {
                    valor = PropertyUtils.getNestedProperty(vo, grafo);
                } catch (NestedNullException e) {
                }
                f.set(campo, valor == null ? "" : valor.toString());
            }
        } catch (Exception e) {
            throw new PlcException("jcompany.error.generic", new Object[] { "montaPropsVOParaFormBean", e }, e, log);
        }
    }

    /**
	 * JCompany. Complementa o ENTITY mestre com VOs de detalhe
	 * 
	 * @param eAlteracao
	 */
    protected PlcBaseEntity montaVOComplementaDetalhes(PlcBaseContextEntity context, PlcActionMapping plcMapping, HttpServletRequest request, DynaActionForm form, PlcBaseEntity vo, boolean eAlteracao) throws PlcException {
        PlcActionMappingDet plcMappingDet = (PlcActionMappingDet) plcMapping;
        try {
            List nomeProps = (List) config.get(plcMappingDet, "detNomePropriedade");
            List nomeClasses = (List) config.get(plcMappingDet, "detVO");
            List propDesp = (List) config.get(plcMapping, "detFlagDesprezar");
            List cardinalidades = (List) config.get(plcMapping, "detCardinalidade");
            List descZero = (List) config.get(plcMapping, "detDescZero");
            List testaDuplicatas = (List) config.get(plcMapping, "detTestaDuplicata");
            Iterator i = nomeProps.iterator();
            Iterator x = nomeClasses.iterator();
            Iterator z = propDesp.iterator();
            Iterator w = cardinalidades.iterator();
            Iterator u = descZero.iterator();
            Iterator v = testaDuplicatas.iterator();
            String nomeProp = "";
            String nomeClasse = "";
            String nomePropDesprezar = "";
            String cardinalidade = "";
            String testaDuplicata = "";
            ArrayList listaDetalhesValidos = new ArrayList();
            while (i.hasNext()) {
                nomeProp = (String) i.next();
                nomeClasse = (String) x.next();
                nomePropDesprezar = (String) z.next();
                cardinalidade = (String) w.next();
                testaDuplicata = (String) v.next();
                boolean zeroSignificativo = ((String) u.next()).equals("");
                List listaOrigem = (List) form.get(nomeProp);
                Object[] ret = montaVOComplDetalhesMontaPai(context, plcMapping, listaOrigem, vo, nomePropDesprezar, nomeClasse, zeroSignificativo, testaDuplicata, request, eAlteracao);
                listaDetalhesValidos = (ArrayList) ret[0];
                Long totDetValidos = (Long) ret[1];
                boolean validouOk = true;
                if (form.get("modePlc") != null && !form.get("modePlc").equals(PlcConstantsCommons.MODES.MODE_EXCLUSION)) {
                    request.setAttribute(PlcConstants.FORM.AUTOMATION.DETAIL.DETAIL_ON_DEMAND, context.getDetailsOnDemand());
                    validouOk = getValidacaoService().validateCardinality(request, cardinalidade, totDetValidos, nomeClasse);
                }
                if (!validouOk) return null;
                if (nomeProp.indexOf("_Det") >= 0) {
                    Set auxS = new HashSet();
                    auxS.addAll(listaDetalhesValidos);
                    String nomePropSemUnderscore = nomeProp.substring(0, nomeProp.indexOf("_"));
                    PropertyUtils.setProperty(vo, nomePropSemUnderscore, auxS);
                } else {
                    if (Set.class.isAssignableFrom(PropertyUtils.getPropertyType(vo, nomeProp))) {
                        Set auxS = new HashSet();
                        auxS.addAll(listaDetalhesValidos);
                        PropertyUtils.setProperty(vo, nomeProp, auxS);
                        montaVOComplementaDetalhesRefMutua(vo, auxS.iterator(), plcMapping.getModalidade());
                    } else {
                        PropertyUtils.setProperty(vo, nomeProp, listaDetalhesValidos);
                        montaVOComplementaDetalhesRefMutua(vo, listaDetalhesValidos.iterator(), plcMapping.getModalidade());
                    }
                }
            }
            return vo;
        } catch (Exception e) {
            throw new PlcException("jcompany.erros.monta.detalhes", new Object[] { e }, e, log);
        }
    }

    /**
	 * Registra referencia do Mestre nos Detalhes, exigidos para alguns
	 * frameworks de persistencia reatacharem apropriadamente o grafo de dominio
	 * (Ex: Hibernate)
	 * 
	 * @since jCompany 3.03
	 * @param vo
	 *            Vo Principal a ser incluido nos detalhes
	 * @param iterator
	 *            Iteracao sobre detalhes para incluir referencia.
	 * @param modalidade Se for M:N entao tem cole��o dos dois lados, senao � um bean apenas
	 */
    protected void montaVOComplementaDetalhesRefMutua(PlcBaseEntity vo, Iterator iterator, String modalidade) throws PlcException {
        try {
            while (iterator.hasNext()) {
                PlcBaseEntity voDet = (PlcBaseEntity) iterator.next();
                if (!PlcAnnotationHelper.getInstance().existsAnnotationManyToMany(voDet.getClass())) PropertyUtils.setProperty(voDet, vo.getPropertyNamePlc(), vo); else {
                    if (Set.class.isAssignableFrom(PropertyUtils.getPropertyType(voDet, vo.getPropertyNamePlc()))) {
                        Set s = (Set) PropertyUtils.getProperty(voDet, vo.getPropertyNamePlc());
                        if (s == null) s = new HashSet();
                        s.add(vo);
                    } else {
                        List l = (List) PropertyUtils.getProperty(voDet, vo.getPropertyNamePlc());
                        if (l == null) l = new ArrayList();
                        l.add(vo);
                    }
                }
            }
        } catch (Exception e) {
            throw new PlcException("jcompany.error.generic", new Object[] { "montaVOComplementaDetalhesRefMutua", e }, e, log);
        }
    }

    /**
	 * JCompany. Monta os Sets de detalhe nos campos ArrayList dos form-beans
	 * Struts
	 */
    public void complementaDetalhesForm(PlcActionMapping plcMapping, PlcBaseEntity vo, DynaActionForm f, HttpServletRequest request) throws PlcException {
        log.debug("########## Entrou para complementar Form com Detalhes");
        PlcActionMappingDet plcMappingDet = (PlcActionMappingDet) plcMapping;
        try {
            List nomeProps = (List) config.get(plcMapping, "detNomePropriedade");
            List nomeClasses = (List) config.get(plcMapping, "detVO");
            List comparadores = (List) config.get(plcMapping, "detComparator");
            Iterator i = nomeProps.iterator();
            Iterator j = nomeClasses.iterator();
            Iterator k = comparadores.iterator();
            String nomeProp = "";
            String nomeClasse = "";
            String comparadorClasse = "";
            while (i.hasNext()) {
                nomeProp = (String) i.next();
                nomeClasse = (String) j.next();
                comparadorClasse = (String) k.next();
                List listaOrigem = new ArrayList();
                Set det = null;
                if (nomeProp.indexOf("_Det") >= 0) {
                    String nomePropSemUnderscore = nomeProp.substring(0, nomeProp.indexOf("_"));
                    det = (Set) PropertyUtils.getProperty(vo, nomePropSemUnderscore);
                    if (det != null) listaOrigem.addAll(det);
                } else {
                    if (config.get(plcMapping, "detailsOnDemand") != null && ((Map<String, Class>) config.get(plcMapping, "detailsOnDemand")).containsKey(nomeProp)) {
                        if (log.isDebugEnabled()) log.debug("Nao monta " + nomeProp + " porque � por demanda");
                    } else {
                        if (Set.class.isAssignableFrom(PropertyUtils.getPropertyType(vo, nomeProp))) {
                            det = (Set) PropertyUtils.getProperty(vo, nomeProp);
                            if (det != null) listaOrigem.addAll(det);
                        } else {
                            listaOrigem = (List) PropertyUtils.getProperty(vo, nomeProp);
                        }
                    }
                }
                if (request != null && !"S".equals(PlcConfigControlHelper.getInstance().get(PlcConstants.CONTEXTPARAM.INI_DESACTIVATE_COMPARATOR_DETAILS)) && !comparadorClasse.equals("#")) {
                    Comparator comp = (Comparator) Class.forName(comparadorClasse).newInstance();
                    Collections.sort(listaOrigem, comp);
                }
                f.set(nomeProp, listaOrigem);
                if (log.isDebugEnabled()) log.debug("Montou ok o detalhe=" + nomeProp);
            }
        } catch (Exception e) {
            throw new PlcException("jcompany.erros.monta.detalhes", new Object[] { e }, e, log);
        }
    }

    /**
	 * Recupera ENTITY anterior da sess�o, diferenciando esquema de tabular ou
	 * crud-mestredetalhe
	 * 
	 * @return ENTITY Anterior ou null, se n�o encontrou
	 */
    public PlcBaseEntity recuperaVOAnteriorSessao(HttpServletRequest request, String logica, String nomeVO, PlcBaseEntity vo) {
        log.debug("######## Entrou em recuperaVOAnteriorSessao");
        if (!(logica.equals(PlcConstantsCommons.DEFAULT_LOGIC.DP.PATTERN_TABULAR) || logica.equals(PlcConstantsCommons.DEFAULT_LOGIC.DP.PATTERN_CRUD_TABULAR)) && request.getSession().getAttribute(PlcConstantsCommons.ENTITY.PREFIX_OBJ + nomeVO) != null) return (PlcBaseEntity) request.getSession().getAttribute(PlcConstantsCommons.ENTITY.PREFIX_OBJ + nomeVO); else if ((logica.equals(PlcConstantsCommons.DEFAULT_LOGIC.DP.PATTERN_TABULAR) || logica.equals(PlcConstantsCommons.DEFAULT_LOGIC.DP.PATTERN_CRUD_TABULAR)) && request.getSession().getAttribute(PlcConstantsCommons.ENTITY.PREFIX_OBJ + nomeVO + PlcConstants.SUFFIX_LIST) != null && vo != null) {
            List l = (List) request.getSession().getAttribute(PlcConstantsCommons.ENTITY.PREFIX_OBJ + nomeVO + PlcConstants.SUFFIX_LIST);
            PlcEntityHelper voH = PlcEntityHelper.getInstance();
            return (PlcBaseEntity) voH.returnEntityEntirelyEqual(l, vo);
        } else return null;
    }

    /**
	 * Recupera ENTITY anterior da sess�o, utilizando padr�o de nome:
	 * PlcConstantsCommons.ENTITY.PREFIX_OBJ+nomeFinalVO
	 */
    public PlcBaseEntity recuperaVOAnteriorSessao(HttpServletRequest request, String nomeValueObject) {
        log.debug("######## Entrou em recuperaVOAnteriorSessao");
        String nomeVO = nomeValueObject.substring(nomeValueObject.lastIndexOf(".") + 1);
        return (PlcBaseEntity) request.getSession().getAttribute(PlcConstantsCommons.ENTITY.PREFIX_OBJ + nomeVO);
    }
}
