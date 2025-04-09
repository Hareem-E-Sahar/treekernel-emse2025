package org.humboldt.cassia.zk.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.humboldt.cassia.core.FacadeConsultasConjuntosCassiaCore;
import org.humboldt.cassia.core.jdo.CarpetaTematica;
import org.humboldt.cassia.core.jdo.Conjunto;
import org.humboldt.cassia.core.jdo.Homologacion;
import org.humboldt.cassia.core.jdo.PerfilDocumentacion;
import org.humboldt.cassia.core.jdo.RecursosUsuario;
import org.humboldt.cassia.core.jdo.Usuario;
import org.humboldt.cassia.zk.util.SessionUtil;
import org.humboldt.cassia.zk.util.ZKUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Window;

public class CarpetasAction {

    public void editarCarpeta(Component componente) {
        CarpetaTematica carpeta = (CarpetaTematica) componente.getAttribute("carpeta");
        if (carpeta != null) {
            Window win = (Window) Executions.createComponents("/recursos/editar.zul", null, null);
            try {
                ((Button) win.getFellow("btnActRecurso")).setAttribute("carpeta", carpeta);
                ((Textbox) win.getFellow("txt_nombre_recurso")).setText(carpeta.getNombre());
                win.doModal();
            } catch (InterruptedException e) {
            }
        } else {
            try {
                Messagebox.show(Labels.getLabel("msg_carpeta_noseleccionado"), Labels.getLabel("msg_titulo_carpeta_seleccionado"), Messagebox.OK, Messagebox.QUESTION);
            } catch (InterruptedException e) {
            }
        }
    }

    public void actualizarCarpeta(Component componente) {
        CarpetaTematica carpeta = (CarpetaTematica) componente.getAttribute("carpeta");
        carpeta.setNombre(((Textbox) componente.getRoot().getFellow("txt_nombre_recurso")).getText());
        SessionUtil.getFacadeOperacionesRecursosCassiaCore().actualizarCarpeta(carpeta);
        try {
            ZKUtil.actualizarArbolCarpetas(componente);
            ((Window) componente.getRoot()).setVisible(false);
            Messagebox.show(Labels.getLabel("msg_carpeta_actualizada"), Labels.getLabel("title_editar_recurso"), Messagebox.OK, Messagebox.QUESTION);
        } catch (InterruptedException e) {
        }
    }

    public void exportarCarpeta(Component componente) {
        CarpetaTematica carpeta = (CarpetaTematica) componente.getAttribute("carpeta");
        if (carpeta != null) {
            try {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ZipOutputStream zipOutput = new ZipOutputStream(byteOut);
                zipOutput.setLevel(6);
                FacadeConsultasConjuntosCassiaCore facade = SessionUtil.getFacadeConsultasConjuntosCassiaCore();
                ArrayList<Conjunto> contenido = new ArrayList(facade.consultarConjuntos(carpeta.getId(), SessionUtil.getUsuario(componente.getDesktop().getSession())));
                HashMap<Long, PerfilDocumentacion> perfiles = new HashMap<Long, PerfilDocumentacion>();
                for (Conjunto conjuntoHijo : contenido) {
                    conjuntoHijo = SessionUtil.getFacadeConsultasConjuntosCassiaCore().consultarConjunto(conjuntoHijo.getId());
                    PerfilDocumentacion perfil = perfiles.get(conjuntoHijo.getPerfil().getId());
                    if (perfil == null) {
                        perfil = SessionUtil.getFacadeConsultasPerfilesCassiaCore().consultarPerfil(conjuntoHijo.getPerfil());
                        perfiles.put(perfil.getId(), perfil);
                    }
                    String xml = SessionUtil.getFacadeConsultasConjuntosCassiaCore().exportarConjunto(conjuntoHijo, perfil);
                    byte buf[] = xml.getBytes();
                    CRC32 crc = new CRC32();
                    ZipEntry zipEntry = new ZipEntry(conjuntoHijo.getNombre() + ".xml");
                    zipEntry.setSize(buf.length);
                    crc.reset();
                    crc.update(buf);
                    zipEntry.setCrc(crc.getValue());
                    zipOutput.putNextEntry(zipEntry);
                    zipOutput.write(buf, 0, buf.length);
                }
                zipOutput.finish();
                zipOutput.close();
                Filedownload filedownload = new Filedownload();
                filedownload.save(new ByteArrayInputStream(byteOut.toByteArray()), "application/zip", "Conjuntos " + carpeta.getNombre() + ".zip");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                Messagebox.show(Labels.getLabel("msg_carpeta_noseleccionado"), Labels.getLabel("msg_titulo_carpeta_seleccionado"), Messagebox.OK, Messagebox.QUESTION);
            } catch (InterruptedException e) {
            }
        }
    }

    public void crearCarpeta(Component componente) {
        CarpetaTematica carpeta = new CarpetaTematica(((Textbox) componente.getRoot().getFellow("txt_nombre_recurso")).getText());
        if (((Checkbox) componente.getRoot().getFellow("chkHija")).isChecked()) {
            String carpetaSel = (String) ((Textbox) componente.getRoot().getFellow("txt_carpeta_conjunto")).getAttribute("id");
            if (carpetaSel != null) {
                CarpetaTematica carpetaPadre = new CarpetaTematica();
                carpetaPadre.setId(Long.parseLong(carpetaSel));
                carpeta.setCarpetaPadre(carpetaPadre);
            }
        }
        SessionUtil.getFacadeOperacionesRecursosCassiaCore().crearCarpeta(carpeta, SessionUtil.getUsuario(componente.getDesktop().getSession()));
        try {
            Messagebox.show(Labels.getLabel("msg_carpeta_creada"), Labels.getLabel("title_nuevo_recurso"), Messagebox.OK, Messagebox.QUESTION);
        } catch (InterruptedException e) {
        }
        ZKUtil.actualizarArbolCarpetas(componente);
    }

    public void eliminarCarpeta(Component componente) {
        CarpetaTematica carpeta = (CarpetaTematica) componente.getAttribute("carpeta");
        if (carpeta != null) {
            Collection carpetas = SessionUtil.getFacadeConsultasConjuntosCassiaCore().consultarCarpetas(carpeta.getId(), null);
            Collection conjuntos = SessionUtil.getFacadeConsultasConjuntosCassiaCore().consultarConjuntos(carpeta.getId(), null);
            if (carpetas.size() > 0 || conjuntos.size() > 0) {
                try {
                    Messagebox.show(Labels.getLabel("msg_carpeta_con_hijos_no_eliminar"), Labels.getLabel("msg_titulo_eliminar_carpeta"), Messagebox.OK, Messagebox.QUESTION);
                    return;
                } catch (InterruptedException e) {
                }
            } else {
                componente = componente.getParent();
                SessionUtil.getFacadeOperacionesRecursosCassiaCore().eliminarCarpeta(carpeta);
                ZKUtil.actualizarArbolCarpetas(componente);
                try {
                    Messagebox.show(Labels.getLabel("msg_carpeta_eliminada"), Labels.getLabel("msg_titulo_eliminar_carpeta"), Messagebox.OK, Messagebox.QUESTION);
                    return;
                } catch (InterruptedException e) {
                }
            }
        } else {
            try {
                Messagebox.show(Labels.getLabel("msg_carpeta_noseleccionado"), Labels.getLabel("msg_titulo_carpeta_seleccionado"), Messagebox.OK, Messagebox.QUESTION);
            } catch (InterruptedException e) {
            }
        }
    }

    public void asociarRecursos(Component componente) {
        Tree arbolCarpetas = (Tree) componente.getRoot().getFellow("arbolCarpetas");
        Usuario usuario = (Usuario) componente.getAttribute("usuario");
        HashMap<Long, Long> carpetas = new HashMap<Long, Long>();
        HashMap<Long, Long> conjuntos = new HashMap<Long, Long>();
        cargarRecursosUsuario(usuario.getId(), carpetas, conjuntos, new HashMap<Long, Long>());
        if (arbolCarpetas.getItems() != null) {
            for (Object objItem : arbolCarpetas.getItems()) {
                if (objItem instanceof Treeitem) {
                    obtenerCarpetasConjuntosSeleccionadas(carpetas, conjuntos, (Treeitem) objItem);
                }
            }
        }
        SessionUtil.getFacadeOperacionesRecursosCassiaCore().asociarRecursos(usuario.getId(), carpetas.values(), conjuntos.values());
        try {
            Messagebox.show(Labels.getLabel("msg_recursos_asociados"), Labels.getLabel("title_asociar_recursos"), Messagebox.OK, Messagebox.QUESTION);
            return;
        } catch (InterruptedException e) {
        }
    }

    private void obtenerCarpetasConjuntosSeleccionadas(HashMap<Long, Long> hsmCarpetas, HashMap<Long, Long> hsmConjuntos, Treeitem item) {
        String tipo = (String) item.getAttribute("tipo");
        if ("CONJUNTO".equals(tipo)) {
            if (!item.isSelected()) hsmConjuntos.remove((Long) item.getAttribute("id")); else hsmConjuntos.put((Long) item.getAttribute("id"), (Long) item.getAttribute("id"));
        } else {
            if (!item.isSelected()) hsmCarpetas.remove((Long) item.getAttribute("id")); else hsmCarpetas.put((Long) item.getAttribute("id"), (Long) item.getAttribute("id"));
        }
        if (item.getChildren() != null) {
            List items = item.getChildren();
            for (Object objItem : items) {
                if (objItem instanceof Treeitem) {
                    obtenerCarpetasConjuntosSeleccionadas(hsmCarpetas, hsmConjuntos, (Treeitem) objItem);
                }
            }
        }
    }

    public void reasignarRecursos(Component componente) {
        Tree arbolCarpetas = (Tree) componente.getRoot().getFellow("arbolRecursos");
        Combobox comboUsuario = (Combobox) componente.getRoot().getFellow("cmbUsuario");
        if (comboUsuario.getSelectedItem() != null) {
            Usuario usuario = (Usuario) comboUsuario.getSelectedItem().getAttribute("object");
            HashMap<Long, Long> recursos = new HashMap<Long, Long>();
            cargarRecursosUsuario(usuario.getId(), new HashMap<Long, Long>(), new HashMap<Long, Long>(), recursos);
            if (arbolCarpetas.getItems() != null) {
                for (Object objItem : arbolCarpetas.getItems()) {
                    if (objItem instanceof Treeitem) {
                        obtenerCarpetasConjuntosSeleccionadas(recursos, recursos, (Treeitem) objItem);
                    }
                }
            }
            SessionUtil.getFacadeOperacionesRecursosCassiaCore().reasignarRecursos(usuario.getId(), recursos.values());
            try {
                Messagebox.show(Labels.getLabel("msg_recursos_reasignados"), Labels.getLabel("title_reasignar_recursos"), Messagebox.OK, Messagebox.QUESTION);
                return;
            } catch (InterruptedException e) {
            }
        }
    }

    private void cargarRecursosUsuario(Long idUsuario, HashMap<Long, Long> hsmCarpetas, HashMap<Long, Long> hsmConjuntos, HashMap<Long, Long> recursos) {
        Collection<RecursosUsuario> recursosUsuario = SessionUtil.getFacadeConsultasUsuarioCassiaCore().consultarRecursosUsuario(idUsuario);
        for (RecursosUsuario recursoUsuario : recursosUsuario) {
            if (recursoUsuario.getCarpeta() != null) {
                hsmCarpetas.put(recursoUsuario.getCarpeta().getId(), recursoUsuario.getCarpeta().getId());
                recursos.put(recursoUsuario.getCarpeta().getId(), recursoUsuario.getId());
            } else if (recursoUsuario.getConjunto() != null) {
                hsmConjuntos.put(recursoUsuario.getConjunto().getId(), recursoUsuario.getConjunto().getId());
                recursos.put(recursoUsuario.getConjunto().getId(), recursoUsuario.getId());
            }
        }
    }

    public void homologarConjunto(Component componente) {
        CarpetaTematica carpeta = (CarpetaTematica) componente.getAttribute("carpeta");
        if (carpeta != null) {
            HashMap parametros = new HashMap();
            parametros.put("carpeta", carpeta);
            Window win = (Window) Executions.createComponents("/recursos/homologar.zul", null, parametros);
            try {
                win.getFellow("btn_homologar").setAttribute("carpeta", carpeta);
                win.doModal();
            } catch (InterruptedException e) {
            }
        } else {
            try {
                Messagebox.show(Labels.getLabel("msg_carpeta_noseleccionado"), Labels.getLabel("msg_titulo_carpeta_seleccionado"), Messagebox.OK, Messagebox.QUESTION);
            } catch (InterruptedException e) {
            }
        }
    }

    public void homologar(Component componente) {
        Collection<Conjunto> conjuntos = new ArrayList<Conjunto>();
        Grid gridConjuntos = (Grid) componente.getRoot().getFellow("gridConjuntosHomo");
        obtenerConjuntosSeleccionadas(gridConjuntos, conjuntos);
        if (((Combobox) componente.getRoot().getFellow("cmbHomologacion")).getSelectedItem() != null) {
            for (Conjunto conjunto : conjuntos) {
                Homologacion homologacion = ((Homologacion) ((Combobox) componente.getRoot().getFellow("cmbHomologacion")).getSelectedItem().getAttribute("object"));
                homologacion = SessionUtil.getFacadeConsultasPerfilesCassiaCore().consultarHomologacion(homologacion);
                PerfilDocumentacion perfil = new PerfilDocumentacion();
                perfil.setId(homologacion.getPerfilDestino());
                Conjunto conjuntoNuevo = new Conjunto();
                String nombre = conjunto.getNombre() + "_homo";
                conjuntoNuevo.setNombre(nombre);
                conjuntoNuevo.setPerfil(perfil);
                conjuntoNuevo.setCarpeta((CarpetaTematica) componente.getAttribute("carpeta"));
                SessionUtil.getFacadeOperacionesConjuntosCassiaCore().homologarConjunto(conjunto, conjuntoNuevo, homologacion, SessionUtil.getUsuario(componente.getDesktop().getSession()));
            }
            ZKUtil.actualizarArbolCarpetas(componente);
            try {
                Messagebox.show(Labels.getLabel("msg_conjunto_homologado"), Labels.getLabel("title_homologar_conjunto"), Messagebox.OK, Messagebox.INFORMATION);
            } catch (InterruptedException e) {
            }
        } else {
            try {
                Messagebox.show(Labels.getLabel("msg_seleccionar_homologacion"), Labels.getLabel("title_homologar_conjunto"), Messagebox.OK, Messagebox.INFORMATION);
            } catch (InterruptedException e) {
            }
        }
    }

    private void obtenerConjuntosSeleccionadas(Component componente, Collection<Conjunto> conjuntos) {
        if (componente instanceof Checkbox) {
            if (((Checkbox) componente).isChecked()) conjuntos.add((Conjunto) componente.getAttribute("conjunto"));
        }
        if (componente.getChildren() != null) {
            List items = componente.getChildren();
            for (Object objItem : items) {
                if (objItem instanceof Component) obtenerConjuntosSeleccionadas((Component) objItem, conjuntos);
            }
        }
    }
}
