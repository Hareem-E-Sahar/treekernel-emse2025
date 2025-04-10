package es.caib.regtel.persistence.util;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.naming.InitialContext;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import es.caib.redose.modelInterfaz.ConstantesRDS;
import es.caib.redose.modelInterfaz.DocumentoRDS;
import es.caib.redose.modelInterfaz.ReferenciaRDS;
import es.caib.redose.persistence.intf.RdsFacade;
import es.caib.redose.persistence.intf.RdsFacadeHome;
import es.caib.regtel.model.ExcepcionRegistroTelematico;
import es.caib.regtel.model.ReferenciaRDSAsientoRegistral;
import es.caib.regtel.model.ResultadoRegistroTelematico;
import es.caib.regtel.model.ValorOrganismo;
import es.caib.regtel.persistence.intf.RegistroTelematicoEJB;
import es.caib.regtel.persistence.intf.RegistroTelematicoEJBHome;
import es.caib.sistra.plugins.firma.FirmaIntf;
import es.caib.util.NifCif;
import es.caib.util.StringUtil;
import es.caib.xml.ConstantesXML;
import es.caib.xml.EstablecerPropiedadException;
import es.caib.xml.registro.factoria.ConstantesAsientoXML;
import es.caib.xml.registro.factoria.FactoriaObjetosXMLRegistro;
import es.caib.xml.registro.factoria.ServicioRegistroXML;
import es.caib.xml.registro.factoria.impl.AsientoRegistral;
import es.caib.xml.registro.factoria.impl.DatosAnexoDocumentacion;
import es.caib.xml.registro.factoria.impl.DatosAsunto;
import es.caib.xml.registro.factoria.impl.DatosInteresado;
import es.caib.xml.registro.factoria.impl.DatosOrigen;
import es.caib.xml.registro.factoria.impl.DireccionCodificada;

/**
 * Helper que permite realizar el proceso de registro de entrada
 * (para registros que no proceden de tramites SISTRA)
 */
public class RegistroEntradaHelper {

    private static Log log = LogFactory.getLog(RegistroSalidaHelper.class);

    private FactoriaObjetosXMLRegistro factReg;

    private AsientoRegistral asiento;

    private DatosOrigen datosOrigen;

    private DatosAsunto datosAsunto;

    private DatosInteresado datosRpte;

    private DatosInteresado datosRpdo;

    private boolean setOficina;

    private boolean setInteresado;

    private boolean setAsunto;

    private List anexos;

    /**
	 * Constructor
	 * @throws Exception
	 */
    public RegistroEntradaHelper() throws Exception {
        factReg = ServicioRegistroXML.crearFactoriaObjetosXML();
        factReg.setEncoding(ConstantesXML.ENCODING);
        initData();
    }

    /**
	 * Establece oficina registro (Obligatorio) 
	 * 
	 * @param organo Organo receptor segun tablas maestras REGWEB (Obligatorio) 
	 * @param oficinaRegistro Oficina registro segun tablas maestras REGWEB (Obligatorio) 
	 */
    public void setOficinaRegistro(String organo, String oficinaRegistro) throws ExcepcionRegistroTelematico {
        try {
            datosOrigen.setCodigoEntidadRegistralOrigen(oficinaRegistro);
            datosAsunto.setCodigoOrganoDestino(organo);
            setOficina = true;
        } catch (EstablecerPropiedadException e) {
            throw new ExcepcionRegistroTelematico("Excepcion estableciendo propiedades", e);
        }
    }

    /**
	 * Establece datos interesado (Obligatorio)
	 * @param nif Nif/Cif (Obligatorio)
	 * @param apellidosNombre Apellidos y nombre (Obligatorio)
	 * @param userSeycon Usuario seycon (Si se refiere a un registro autenticado)
	 * @param codPais Codigo pais de la direccion del interesado (Obligatorio)
	 * @param nomPais nombre pais  de la direccion del interesado  (Obligatorio)
	 * @param codProvincia Codigo provincia de la direccion del interesado (Obligatorio)
	 * @param nomProvincia Nombre provincia de la direccion del interesado (Obligatorio)
	 * @param codLocalidad Codigo localidad de la direccion del interesado (Obligatorio)
	 * @param nomLocalidad Nombre localidad de la direccion del interesado (Obligatorio)
	 */
    public void setDatosInteresado(String nif, String apellidosNombre, String userSeycon, String codPais, String nomPais, String codProvincia, String nomProvincia, String codLocalidad, String nomLocalidad) throws ExcepcionRegistroTelematico {
        try {
            datosRpte = factReg.crearDatosInteresado();
            datosRpte.setTipoInteresado(ConstantesAsientoXML.DATOSINTERESADO_TIPO_REPRESENTANTE);
            if (NifCif.esNIF(nif)) datosRpte.setTipoIdentificacion(new Character(ConstantesAsientoXML.DATOSINTERESADO_TIPO_IDENTIFICACION_NIF)); else if (NifCif.esCIF(nif)) datosRpte.setTipoIdentificacion(new Character(ConstantesAsientoXML.DATOSINTERESADO_TIPO_IDENTIFICACION_CIF)); else if (NifCif.esNIE(nif)) datosRpte.setTipoIdentificacion(new Character(ConstantesAsientoXML.DATOSINTERESADO_TIPO_IDENTIFICACION_NIE));
            datosRpte.setNumeroIdentificacion(nif);
            datosRpte.setFormatoDatosInteresado(ConstantesAsientoXML.DATOSINTERESADO_FORMATODATOSINTERESADO_APENOM);
            datosRpte.setIdentificacionInteresado(apellidosNombre);
            datosRpte.setUsuarioSeycon(userSeycon);
            DireccionCodificada dir = factReg.crearDireccionCodificada();
            datosRpte.setDireccionCodificada(dir);
            dir.setPaisOrigen(codPais);
            dir.setCodigoProvincia(codProvincia);
            dir.setNombreProvincia(nomProvincia);
            dir.setCodigoMunicipio(codLocalidad);
            dir.setNombreMunicipio(nomLocalidad);
            setInteresado = true;
        } catch (EstablecerPropiedadException e) {
            throw new ExcepcionRegistroTelematico("Excepcion estableciendo propiedades", e);
        }
    }

    /**
	 * En caso de que el interesado sea el representante establece los datos del representado (Opcional: depende si hay representacion)
	 * @param nif Nif/Cif representado (Obligatorio)
	 * @param apellidosNombre Apellidos y nombre  (Obligatorio)
	 */
    public void setDatosRepresentado(String nif, String apellidosNombre) throws ExcepcionRegistroTelematico {
        try {
            datosRpdo = factReg.crearDatosInteresado();
            datosRpdo.setTipoInteresado(ConstantesAsientoXML.DATOSINTERESADO_TIPO_REPRESENTADO);
            datosRpdo.setNumeroIdentificacion(nif);
            datosRpdo.setFormatoDatosInteresado(ConstantesAsientoXML.DATOSINTERESADO_FORMATODATOSINTERESADO_APENOM);
            datosRpdo.setIdentificacionInteresado(apellidosNombre);
        } catch (EstablecerPropiedadException e) {
            throw new ExcepcionRegistroTelematico("Excepcion estableciendo propiedades", e);
        }
    }

    /**
	 * Datos del asunto
	 * 
	 * @param unidadAdministrativa  (Obligatorio)
	 * @param codigoIdioma  (Obligatorio)
	 * @param tipoAsunto  (Obligatorio)
	 * @param extractoAsunto  (Obligatorio)
	 */
    public void setDatosAsunto(String unidadAdministrativa, String codigoIdioma, String tipoAsunto, String extractoAsunto) throws ExcepcionRegistroTelematico {
        try {
            datosAsunto.setIdiomaAsunto(codigoIdioma);
            datosAsunto.setTipoAsunto(tipoAsunto);
            datosAsunto.setExtractoAsunto(extractoAsunto);
            datosAsunto.setCodigoUnidadAdministrativa(unidadAdministrativa);
            setAsunto = true;
        } catch (EstablecerPropiedadException e) {
            throw new ExcepcionRegistroTelematico("Excepcion estableciendo propiedades", e);
        }
    }

    /**
	 * A�ade documento (Opcional: depende de si desea anexar un documento)
	 *
	 * @param modelo (Obligatorio)
	 * @param version (Obligatorio)
	 * @param contenido (Obligatorio)
	 * @param nombre (Obligatorio)
	 * @param plantilla (Opcional. Solo tiene sentido para modelso estructurados.)
	 * @param firmas (Opcional)
	 */
    public void addDocumento(String modelo, int version, byte[] contenido, String nombre, String extension, String plantilla, FirmaIntf firmas[]) throws ExcepcionRegistroTelematico {
        DocumentoRDS doc = new DocumentoRDS();
        doc.setModelo(modelo);
        doc.setVersion(version);
        String nomfic = "";
        try {
            nomfic = StringUtil.normalizarNombreFichero(nombre) + "." + extension;
        } catch (Exception ex) {
            nomfic = nombre + "." + extension;
        }
        doc.setNombreFichero(nomfic);
        doc.setExtensionFichero(extension);
        doc.setDatosFichero(contenido);
        doc.setFirmas(firmas);
        doc.setPlantilla(plantilla);
        doc.setTitulo(nombre);
        anexos.add(doc);
    }

    /**
	 * A�ade documento ya existente en RDS (Opcional: depende de si desea anexar un documento)
	 * 
	 * @param doc (Obligatorio)
	 */
    public void addDocumento(ReferenciaRDS ref) throws ExcepcionRegistroTelematico {
        anexos.add(ref);
    }

    /**
	 * Realiza proceso de registro a partir de los datos suministrados
	 * @param providerUrl Url EJB Registro Telematico
	 * @param ReferenciaRDSAsientoRegistral Referencia RDS del asiento y sus anexos
	 * @return ResultadoRegistroTelematico
	 * @throws ExcepcionRegistroTelematico
	 */
    public ResultadoRegistroTelematico registrar(String providerUrl, ReferenciaRDSAsientoRegistral refAsiento) throws ExcepcionRegistroTelematico {
        try {
            RegistroTelematicoEJB regtelEJB = getRegistroTelematicoEJB(providerUrl);
            ResultadoRegistroTelematico res = regtelEJB.registroEntrada(refAsiento.getAsientoRegistral(), refAsiento.getAnexos());
            log.debug("Registro entrada efectuado: " + res.getResultadoRegistro().getNumeroRegistro());
            return res;
        } catch (Exception ex) {
            throw new ExcepcionRegistroTelematico("Excepcion en proceso de registro", ex);
        }
    }

    /**
	 * Genera asiento registral y lo inserta en el RDS junto con los anexos 
	 * @param providerUrl
	 * @return Referencia del asiento y sus anexos
	 * @throws ExcepcionRegistroTelematico 
	 */
    public ReferenciaRDSAsientoRegistral generarAsientoRegistral(String providerUrl) throws ExcepcionRegistroTelematico {
        try {
            if (!setOficina) {
                throw new ExcepcionRegistroTelematico("Debe establecerse el metodo setOficinaRegistro");
            }
            if (!setInteresado) {
                throw new ExcepcionRegistroTelematico("Debe establecerse el metodo setDatosInteresado");
            }
            if (!setAsunto) {
                throw new ExcepcionRegistroTelematico("Debe establecerse el metodo setDatosAsunto");
            }
            RegistroTelematicoEJB regtelEJB = getRegistroTelematicoEJB(providerUrl);
            RdsFacade redoseEJB = getRdsEJB(providerUrl);
            datosAsunto.setFechaAsunto(new Date());
            boolean enc = false;
            try {
                List svd = regtelEJB.obtenerServiciosDestino();
                for (Iterator it = svd.iterator(); it.hasNext(); ) {
                    ValorOrganismo vo = (ValorOrganismo) it.next();
                    if (vo.getCodigo().equals(datosAsunto.getCodigoOrganoDestino())) {
                        datosAsunto.setDescripcionOrganoDestino(vo.getDescripcion());
                        enc = true;
                        break;
                    }
                }
            } catch (Exception e) {
                throw new ExcepcionRegistroTelematico("Error al consultar organos destino", e);
            }
            if (!enc) {
                throw new ExcepcionRegistroTelematico("No se encuentra organo destino con codigo " + datosAsunto.getCodigoOrganoDestino());
            }
            asiento.getDatosInteresado().add(datosRpte);
            if (datosRpdo != null) {
                asiento.getDatosInteresado().add(datosRpdo);
            }
            Object anexo;
            for (Iterator it = anexos.iterator(); it.hasNext(); ) {
                anexo = it.next();
                if (anexo instanceof DocumentoRDS) {
                    DocumentoRDS docRDSAnexo = (DocumentoRDS) anexo;
                    docRDSAnexo.setUnidadAdministrativa(Long.parseLong(datosAsunto.getCodigoUnidadAdministrativa(), 10));
                    docRDSAnexo.setUsuarioSeycon(datosRpte.getUsuarioSeycon());
                    docRDSAnexo.setNif(datosRpte.getNumeroIdentificacion());
                }
            }
            testAsiento(redoseEJB);
            ReferenciaRDS ref;
            Map refDocs = new HashMap();
            int i = 0;
            DocumentoRDS docRDSAnexo;
            for (Iterator it = anexos.iterator(); it.hasNext(); ) {
                i++;
                anexo = it.next();
                docRDSAnexo = null;
                if (anexo instanceof DocumentoRDS) {
                    docRDSAnexo = (DocumentoRDS) anexo;
                    ref = redoseEJB.insertarDocumento(docRDSAnexo);
                    docRDSAnexo.setReferenciaRDS(ref);
                } else if (anexo instanceof ReferenciaRDS) {
                    ref = (ReferenciaRDS) anexo;
                    docRDSAnexo = redoseEJB.consultarDocumento(ref);
                } else {
                    throw new Exception("La clase del anexo deber ser DocumentoRDS o ReferenciaRDS");
                }
                DatosAnexoDocumentacion datosAnexo = crearDatosAnexoDocumentacion(docRDSAnexo, "ANEXO-" + i, ConstantesAsientoXML.DATOSANEXO_OTROS, docRDSAnexo.isEstructurado());
                datosAnexo.setCodigoRDS(Long.toString(ref.getCodigo()));
                asiento.getDatosAnexoDocumentacion().add(datosAnexo);
                refDocs.put(datosAnexo.getIdentificadorDocumento(), docRDSAnexo.getReferenciaRDS());
            }
            String xmlAsiento = factReg.guardarAsientoRegistral(asiento);
            log.debug("Asiento registral generado: \n" + xmlAsiento);
            DocumentoRDS docRDSAsiento = new DocumentoRDS();
            docRDSAsiento.setModelo(ConstantesRDS.MODELO_ASIENTO_REGISTRAL);
            docRDSAsiento.setVersion(ConstantesRDS.VERSION_ASIENTO);
            docRDSAsiento.setNombreFichero("Asiento.xml");
            docRDSAsiento.setExtensionFichero("xml");
            docRDSAsiento.setDatosFichero(xmlAsiento.getBytes(ConstantesXML.ENCODING));
            docRDSAsiento.setTitulo("Asiento registral");
            docRDSAsiento.setUnidadAdministrativa(Long.parseLong(datosAsunto.getCodigoUnidadAdministrativa(), 10));
            docRDSAsiento.setUsuarioSeycon(datosRpte.getUsuarioSeycon());
            docRDSAsiento.setNif(datosRpte.getNumeroIdentificacion());
            ref = redoseEJB.insertarDocumento(docRDSAsiento);
            ReferenciaRDSAsientoRegistral refAsiento = new ReferenciaRDSAsientoRegistral();
            refAsiento.setAsientoRegistral(ref);
            refAsiento.setAnexos(refDocs);
            return refAsiento;
        } catch (Exception ex) {
            throw new ExcepcionRegistroTelematico("Excepcion generando asiento registral", ex);
        }
    }

    /**
	 * Posteriormente a registrar podemos obtener las referencias RDS de los documentos anexados 
	 * @param index Indice del documento (empieza desde 0)
	 * @return
	 */
    public ReferenciaRDS getReferenciaDocumento(int index) throws ExcepcionRegistroTelematico {
        if (index >= anexos.size()) throw new ExcepcionRegistroTelematico("No existe documento con indice " + index);
        if (anexos.get(index) instanceof DocumentoRDS) {
            DocumentoRDS doc = (DocumentoRDS) anexos.get(index);
            return doc.getReferenciaRDS();
        } else if (anexos.get(index) instanceof ReferenciaRDS) {
            return (ReferenciaRDS) anexos.get(index);
        } else {
            throw new ExcepcionRegistroTelematico("La clase del anexo deber ser DocumentoRDS o ReferenciaRDS");
        }
    }

    /**
	 * Resetea informacion para reutilizar helper para nuevo registro 
	 * @param index
	 * @return
	 */
    public void reset() throws ExcepcionRegistroTelematico {
        try {
            initData();
        } catch (Exception ex) {
            throw new ExcepcionRegistroTelematico("Excepcion creando objetos", ex);
        }
    }

    private void initData() throws Exception {
        asiento = factReg.crearAsientoRegistral();
        datosOrigen = factReg.crearDatosOrigen();
        datosOrigen.setTipoRegistro(new Character(ConstantesAsientoXML.TIPO_REGISTRO_ENTRADA));
        datosOrigen.setNumeroRegistro("");
        asiento.setDatosOrigen(datosOrigen);
        datosAsunto = factReg.crearDatosAsunto();
        datosAsunto.setExtractoAsunto("Registro entrada");
        asiento.setDatosAsunto(datosAsunto);
        asiento.setVersion("1.0");
        anexos = new ArrayList();
        setOficina = false;
        setInteresado = false;
        setAsunto = false;
    }

    /**
	 * Generamos asiento de test para detectar incoherencias
	 * @param anexoOficio 
	 * @param anexoAviso 
	 * @throws Exception
	 */
    private void testAsiento(RdsFacade redoseEJB) throws Exception {
        try {
            int i = 0;
            Object anexoTipo;
            for (Iterator it = anexos.iterator(); it.hasNext(); ) {
                i++;
                anexoTipo = it.next();
                if (anexoTipo instanceof DocumentoRDS) {
                    DocumentoRDS anexo = (DocumentoRDS) anexoTipo;
                    DatosAnexoDocumentacion datosAnexo = crearDatosAnexoDocumentacion(anexo, "ANEXO-" + i, ConstantesAsientoXML.DATOSANEXO_OTROS, anexo.isEstructurado());
                    datosAnexo.setCodigoRDS("test");
                    asiento.getDatosAnexoDocumentacion().add(datosAnexo);
                } else if (anexoTipo instanceof ReferenciaRDS) {
                    ReferenciaRDS refAnexo = (ReferenciaRDS) anexoTipo;
                    DocumentoRDS anexo = redoseEJB.consultarDocumento(refAnexo, false);
                    DatosAnexoDocumentacion datosAnexo = crearDatosAnexoDocumentacion(anexo, "ANEXO-" + i, ConstantesAsientoXML.DATOSANEXO_OTROS, anexo.isEstructurado());
                    datosAnexo.setCodigoRDS("test");
                    asiento.getDatosAnexoDocumentacion().add(datosAnexo);
                }
            }
            String xmlAsiento = factReg.guardarAsientoRegistral(asiento);
            factReg.crearAsientoRegistral(new ByteArrayInputStream(xmlAsiento.getBytes(ConstantesXML.ENCODING)));
        } finally {
            asiento.getDatosAnexoDocumentacion().clear();
        }
    }

    /**
	 * Crea datos anexo a partir de la informacion suministrada
	 * @param anexo
	 * @param identificador
	 * @param tipo
	 * @param estructurado
	 * @return
	 * @throws EstablecerPropiedadException
	 */
    private DatosAnexoDocumentacion crearDatosAnexoDocumentacion(DocumentoRDS anexo, String identificador, char tipo, boolean estructurado) throws Exception {
        DatosAnexoDocumentacion datosAnexo;
        datosAnexo = factReg.crearDatosAnexoDocumentacion();
        datosAnexo.setTipoDocumento(new Character(tipo));
        datosAnexo.setIdentificadorDocumento(identificador);
        datosAnexo.setCodigoNormalizado(anexo.getModelo() + "-" + anexo.getVersion());
        datosAnexo.setNombreDocumento(anexo.getTitulo());
        datosAnexo.setEstucturado(new Boolean(estructurado));
        datosAnexo.setExtractoDocumento(anexo.getTitulo());
        if (anexo.getHashFichero() != null && !"".equals(anexo.getHashFichero())) {
            datosAnexo.setHashDocumento(anexo.getHashFichero());
        } else {
            datosAnexo.setHashDocumento(generaHash(anexo.getDatosFichero()));
        }
        return datosAnexo;
    }

    private RegistroTelematicoEJB getRegistroTelematicoEJB(String providerUrl) throws ExcepcionRegistroTelematico {
        try {
            Properties configuracion = getConfiguracion(providerUrl);
            InitialContext initialContext = new InitialContext(configuracion);
            Object objRef = initialContext.lookup(RegistroTelematicoEJBHome.JNDI_NAME);
            RegistroTelematicoEJBHome homeRegistro = (RegistroTelematicoEJBHome) javax.rmi.PortableRemoteObject.narrow(objRef, RegistroTelematicoEJBHome.class);
            RegistroTelematicoEJB registroTelematicoEJB = homeRegistro.create();
            return registroTelematicoEJB;
        } catch (Exception ex) {
            throw new ExcepcionRegistroTelematico("Excepcion obteniendo referencia EJB del Registro Telematico", ex);
        }
    }

    private RdsFacade getRdsEJB(String providerUrl) throws ExcepcionRegistroTelematico {
        try {
            Properties configuracion = getConfiguracion(providerUrl);
            InitialContext initialContext = new InitialContext(configuracion);
            Object objRef = initialContext.lookup(RdsFacadeHome.JNDI_NAME);
            RdsFacadeHome homeRDS = (RdsFacadeHome) javax.rmi.PortableRemoteObject.narrow(objRef, RdsFacadeHome.class);
            RdsFacade rdsEJB = homeRDS.create();
            return rdsEJB;
        } catch (Exception ex) {
            throw new ExcepcionRegistroTelematico("Excepcion obteniendo referencia EJB del REDOSE", ex);
        }
    }

    private Properties getConfiguracion(String providerUrl) throws ExcepcionRegistroTelematico {
        Properties configuracion = new Properties();
        if (!providerUrl.equals("LOCAL")) {
            if (providerUrl.startsWith("http")) {
                configuracion.put("java.naming.factory.initial", "org.jboss.naming.HttpNamingContextFactory");
            } else if (providerUrl.startsWith("jnp")) {
                configuracion.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
            } else {
                throw new ExcepcionRegistroTelematico("Provider debe ser jnp o http");
            }
            configuracion.put("java.naming.provider.url", providerUrl);
            configuracion.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
        }
        return configuracion;
    }

    private String generaHash(byte[] datos) throws Exception {
        MessageDigest dig = MessageDigest.getInstance(ConstantesRDS.HASH_ALGORITMO);
        return new String(Hex.encodeHex(dig.digest(datos)));
    }
}
