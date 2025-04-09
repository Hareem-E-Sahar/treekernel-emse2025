package org.persistente.esquema;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.persistente.PersistenteException;

/**
 * Representa uma relação atributo-campo.
 */
@SuppressWarnings("unchecked")
public abstract class CampoAbstrato<T> {

    private Field campo;

    public CampoAbstrato(Field campo) {
        this.campo = campo;
    }

    public CampoAbstrato<T> novo(Field campo) {
        try {
            return this.getClass().getConstructor(Field.class).newInstance(campo);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getNome() {
        return campo.getName();
    }

    public abstract T get(ResultSet rs) throws SQLException;

    public abstract void set(PreparedStatement ps, int ind, T valor) throws SQLException;

    public String tipoSql() {
        return "text";
    }

    public T get(Object obj) {
        try {
            return (T) campo.get(obj);
        } catch (Exception ex) {
            throw new PersistenteException("Não foi possível acessar atributo: " + campo.getName(), ex);
        }
    }

    public void set(Object obj, T val) {
        try {
            campo.set(obj, val);
        } catch (Exception ex) {
            throw new PersistenteException("Não foi possível acessar atributo: " + campo.getName(), ex);
        }
    }
}
