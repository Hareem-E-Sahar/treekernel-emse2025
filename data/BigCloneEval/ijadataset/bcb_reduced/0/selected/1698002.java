package com.doculibre.intelligid.wicket.pages.mesdossiers.dossier;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import com.doculibre.wicket.util.ContextPathResourceReference;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.extensions.markup.html.datepicker.DatePicker;
import wicket.extensions.markup.html.datepicker.DatePickerSettings;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Button;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.DateValidator;
import wicket.markup.html.resources.JavaScriptReference;
import wicket.model.Model;
import wicket.util.convert.IConverter;
import com.doculibre.intelligid.delegate.FGDDelegate;
import com.doculibre.intelligid.entites.FicheDossier;
import com.doculibre.intelligid.entrepot.conversation.ConversationManager;
import com.doculibre.intelligid.utils.FGDDateConverter;
import com.doculibre.intelligid.utils.FGDDateUtils;
import com.doculibre.intelligid.utils.FGDSpringUtils;
import com.doculibre.intelligid.wicket.components.DatePanel;
import com.doculibre.intelligid.wicket.pages.BaseFGDPage;

@SuppressWarnings("serial")
public class VerserDossierPage extends BaseFGDPage {

    public VerserDossierPage(PageParameters parameters) {
        super(parameters);
        String id = parameters.getString("id");
        FGDDelegate delegate = new FGDDelegate();
        FicheDossier ficheDossier = delegate.getFicheDossier(new Long(id), getUtilisateurCourant());
        initComponents(ficheDossier);
    }

    private void initComponents(final FicheDossier ficheDossier) {
        addObjetNumeriqueMultiRequete(ficheDossier);
        Form form = new Form("form");
        add(form);
        String dateVersementPrevue;
        if (ficheDossier.getDateVersementPrevue() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dateVersementPrevue = sdf.format(ficheDossier.getDateVersementPrevue());
        } else {
            dateVersementPrevue = "";
        }
        form.add(new Label("dateVersementPrevue", dateVersementPrevue));
        final FormComponent field = getField("dateVersementReelle", ficheDossier);
        form.add(field);
        add(new JavaScriptReference("maskjs", DatePanel.class, "masks.js"));
        DatePickerSettings datePickerSettings = new DatePickerSettings();
        datePickerSettings.setIcon(new ContextPathResourceReference("/ui/images/commun/calendrier.gif"));
        DatePicker datePicker = new DatePicker("datePicker", field, datePickerSettings);
        datePicker.setDateConverter(FGDDateUtils.getDateConverter());
        form.add(datePicker);
        form.add(new Button("cancelButton") {

            @Override
            protected void onSubmit() {
                RequestCycle.get().setResponsePage(ConsulterDossierPage.class, new PageParameters("id=" + ficheDossier.getId()));
            }
        });
        form.add(new Button("actionButton") {

            @Override
            protected void onSubmit() {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date date = sdf.parse(field.getValue());
                    if (date.before(ficheDossier.getDateFermeture())) {
                        String fermeture = "(" + sdf.format(ficheDossier.getDateFermeture()) + ")";
                        VerserDossierPage.this.error("La date de versement ne doit pas être inférieure à la date de fermeture " + fermeture);
                    } else {
                        ficheDossier.setDateVersementReelle(date);
                        ficheDossier.setUtilisateurVersementReel(getUtilisateurCourant());
                        new FGDDelegate().sauvegarder((FicheDossier) ficheDossier, getUtilisateurCourant());
                        ConversationManager conversationManager = FGDSpringUtils.getConversationManager();
                        conversationManager.commitTransaction();
                        RequestCycle.get().setResponsePage(ConsulterDossierPage.class, new PageParameters("id=" + ficheDossier.getId()));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected FormComponent getField(String id, final FicheDossier ficheDossier) {
        FormComponent field = new TextField(id, new Model(), Date.class) {

            public IConverter getConverter() {
                return new FGDDateConverter();
            }
        };
        field.add(new DateValidator() {

            @Override
            public void onValidate(FormComponent formComponent, Date value) {
                try {
                    String dateStr = formComponent.getRawInput();
                    if (StringUtils.isNotEmpty(dateStr)) {
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                        formatter.parse(dateStr);
                    }
                } catch (ParseException e) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("date", value);
                    error(formComponent, "StringValidator.dateFormatInvalide", map);
                }
            }
        });
        return field;
    }
}
