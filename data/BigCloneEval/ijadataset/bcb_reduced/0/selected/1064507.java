package g4p.tool.components;

import g4p.tool.Messages;
import g4p.tool.gui.GuiDesigner;
import g4p.tool.gui.propertygrid.EditorBase;
import g4p.tool.gui.propertygrid.EditorJComboBox;
import g4p.tool.gui.propertygrid.EditorJFileChooser;
import g4p.tool.gui.propertygrid.Validator;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;

@SuppressWarnings("serial")
public class DButton extends DCoreText {

    protected transient RectangularShape face;

    protected transient BufferedImage icon = null;

    protected transient float mitre = 6.0f;

    protected int iconWidth, iconHeight, iconX, iconY;

    protected int textWidth, textHeight, textX, textY, textMin, textMax;

    protected int textHAlign, textVAlign, iconAlign, iconAlignModel;

    ;

    protected int style;

    public String _0028_btn_style = "Text only";

    public transient EditorBase btn_style_editor = new EditorJComboBox(BUTTON_STYLE);

    public Boolean btn_style_edit = true;

    public Boolean btn_style_show = true;

    public String btn_style_label = "Button style";

    public String btn_style_updater = "btnStyleChanged";

    public String _0033_xtAlignment = "CENTER";

    public transient EditorBase xtAlignment_editor = new EditorJComboBox(H_ALIGN_3);

    public Boolean xtAlignment_edit = true;

    public Boolean xtAlignment_show = true;

    public String xtAlignment_label = "Horz text alignment";

    public String xtAlignment_updater = "textAlignChanged";

    public String _0034_ytAlignment = "MIDDLE";

    public transient EditorBase ytAlignment_editor = new EditorJComboBox(V_ALIGN);

    public Boolean ytAlignment_edit = true;

    public Boolean ytAlignment_show = true;

    public String ytAlignment_label = "Vert text alignment";

    public String ytAlignment_updater = "textAlignChanged";

    public int _0036_nbr_tiles = 3;

    public Boolean nbr_tiles_edit = true;

    public Boolean nbr_tiles_show = false;

    public String nbr_tiles_label = "Nbr of tiles in icon";

    public String nbr_tiles_updater = "nbrTilesChanged";

    public Validator nbr_tiles_validator = Validator.getValidator(int.class, 1, 3);

    public String _0037_icon_file = "";

    public transient EditorJFileChooser icon_file_editor = new EditorJFileChooser();

    public Boolean icon_file_edit = true;

    public Boolean icon_file_show = false;

    public String icon_file_label = "Icon file";

    public String icon_file_updater = "iconChanged";

    public String _0038_icon_alignment = "LEFT";

    public transient EditorBase icon_alignment_editor = new EditorJComboBox(H_ALIGN_3);

    public Boolean icon_alignment_edit = true;

    public Boolean icon_alignment_show = false;

    public String icon_alignment_label = "Icon alignment";

    public String icon_alignment_updater = "iconAlignChanged";

    public DButton() {
        super();
        componentClass = "GButton";
        set_name(NameGen.instance().getNext("button"));
        set_event_name(NameGen.instance().getNext(get_name() + "_Click"));
        _0029_text = "Face text";
        _0130_width = 80;
        _0131_height = 30;
        text_tooltip = "text to show on button";
        face = new RoundRectangle2D.Float(0, 0, _0130_width, _0131_height, mitre, mitre);
        textWidth = GuiDesigner.metrics().stringWidth(_0029_text);
        textHeight = GuiDesigner.metrics().getHeight();
        style = ListGen.instance().getIndexOf(BUTTON_STYLE, _0028_btn_style);
        textHAlign = ListGen.instance().getIndexOf(H_ALIGN_3, _0033_xtAlignment);
        textVAlign = ListGen.instance().getIndexOf(V_ALIGN, _0034_ytAlignment);
        iconAlign = ListGen.instance().getIndexOf(H_ALIGN_3, _0038_icon_alignment);
        iconAlignModel = H_ALIGN_3;
        calculateAlignmentValues();
    }

    /**
	 * Recalculate the alignment values for drawing this button in the designer.
	 */
    public void updatedInGUI() {
        calculateAlignmentValues();
    }

    public void btnStyleChanged() {
        int newStyle = ListGen.instance().getIndexOf(BUTTON_STYLE, _0028_btn_style);
        switch(newStyle) {
            case TEXT_ONLY:
                text_show = true;
                xtAlignment_show = true;
                ytAlignment_show = true;
                icon_file_show = false;
                nbr_tiles_show = false;
                icon_alignment_show = false;
                break;
            case ICON_ONLY:
                text_show = false;
                xtAlignment_show = false;
                ytAlignment_show = false;
                icon_file_show = true;
                nbr_tiles_show = true;
                icon_alignment_show = true;
                if (style != ICON_ONLY) {
                    icon_alignment_editor = new EditorJComboBox(H_ALIGN_3);
                    iconAlign = (iconAlign == CENTER) ? iconAlign - 1 : 0;
                    iconAlignModel = H_ALIGN_2;
                }
                break;
            case TEXT_AND_ICON:
                text_show = true;
                xtAlignment_show = true;
                ytAlignment_show = true;
                icon_file_show = true;
                nbr_tiles_show = true;
                icon_alignment_show = true;
                if (style != TEXT_AND_ICON) {
                    icon_alignment_editor = new EditorJComboBox(H_ALIGN_2);
                    iconAlign = (iconAlign == 1) ? 2 : 0;
                    iconAlignModel = H_ALIGN_3;
                }
                break;
        }
        style = newStyle;
        propertyModel.createProperties(this);
        propertyModel.hasBeenChanged();
    }

    public void textChanged() {
        textWidth = GuiDesigner.metrics().stringWidth(_0029_text);
        validateButtonSize();
        calculateAlignmentValues();
        propertyModel.hasBeenChanged();
    }

    public void textAlignChanged() {
        textHAlign = ListGen.instance().getIndexOf(H_ALIGN_3, _0033_xtAlignment);
        textVAlign = ListGen.instance().getIndexOf(V_ALIGN, _0034_ytAlignment);
        calculateAlignmentValues();
        propertyModel.hasBeenChanged();
    }

    public void iconChanged() {
        icon = this.getImageFromDataFolder(_0037_icon_file);
        if (icon != null) {
            iconWidth = icon.getWidth() / _0036_nbr_tiles;
            iconHeight = icon.getHeight();
            validateButtonSize();
            calculateAlignmentValues();
            propertyModel.hasBeenChanged();
        }
    }

    public void iconAlignChanged() {
        if (iconAlignModel == H_ALIGN_3) iconAlign = ListGen.instance().getIndexOf(H_ALIGN_3, _0038_icon_alignment); else iconAlign = ListGen.instance().getIndexOf(H_ALIGN_2, _0038_icon_alignment);
        calculateAlignmentValues();
        propertyModel.hasBeenChanged();
    }

    public void nbrTilesChanged() {
        if (icon == null && _0037_icon_file.length() > 0) icon = this.getImageFromDataFolder(_0037_icon_file);
        if (icon != null) {
            iconWidth = icon.getWidth() / _0036_nbr_tiles;
            validateButtonSize();
            calculateAlignmentValues();
            propertyModel.hasBeenChanged();
        }
    }

    protected void validateButtonSize() {
        int w = textWidth, h = textHeight;
        switch(style) {
            case ICON_ONLY:
                w = iconWidth;
                h = iconHeight;
                break;
            case TEXT_AND_ICON:
                w = iconWidth + textWidth;
                h = Math.max(iconHeight, textHeight);
        }
        w = Math.max(_0130_width, iconWidth + textWidth);
        h = Math.max(_0131_height, h);
        if (w != _0130_width || h != _0131_height) {
            _0130_width = w;
            _0131_height = h;
        }
    }

    protected void calculateAlignmentValues() {
        textMin = 0;
        textMax = _0130_width;
        switch(style) {
            case ICON_ONLY:
            case TEXT_AND_ICON:
                iconY = (_0131_height - iconHeight) / 2;
                switch(iconAlign) {
                    case LEFT:
                        iconX = 0;
                        textMin = iconWidth;
                        break;
                    case CENTER:
                        iconX = (_0130_width - iconWidth) / 2;
                        break;
                    case RIGHT:
                        iconX = _0130_width - iconWidth;
                        textMax = iconX;
                        break;
                }
                break;
        }
        if (style == TEXT_ONLY || style == TEXT_AND_ICON) {
            switch(textHAlign) {
                case LEFT:
                    textX = textMin;
                    break;
                case CENTER:
                    textX = textMin + (textMax - textMin - textWidth) / 2;
                    break;
                case RIGHT:
                    textX = textMax - textWidth;
                    break;
            }
            switch(textVAlign) {
                case TOP:
                    textY = textHeight + 1;
                    break;
                case MIDDLE:
                    textY = (_0131_height + textHeight) / 2;
                    break;
                case BOTTOM:
                    textY = _0131_height - 1;
                    break;
            }
        }
    }

    public void draw(Graphics2D g, AffineTransform paf, DBase selected) {
        AffineTransform af = new AffineTransform(paf);
        af.translate(_0120_x, _0121_y);
        g.setTransform(af);
        ((RoundRectangle2D) face).setRoundRect(0, 0, _0130_width, _0131_height, mitre, mitre);
        g.setStroke(stdStroke);
        g.setColor(btnBack);
        g.fill(face);
        if (style == TEXT_ONLY || style == TEXT_AND_ICON && _0029_text.length() > 0) {
            g.setColor(txfFore);
            g.drawString(_0029_text, textX, textY);
        } else {
            g.setColor(blackEdge);
            g.drawString(_0010_name, textX, textY);
        }
        if (style == ICON_ONLY || style == TEXT_AND_ICON && icon != null) {
            g.drawImage(icon, iconX, iconY, iconX + iconWidth, iconY + iconHeight, 0, 0, iconWidth, iconHeight, null);
        }
        g.setColor(blackEdge);
        g.draw(face);
        if (this == selected) drawSelector(g);
        g.setTransform(paf);
    }

    /**
	 * Get the creator statement var = new Foo(...);
	 * @return
	 */
    protected String get_creator(DBase parent) {
        String s = "";
        switch(style) {
            case TEXT_ONLY:
                s = Messages.build(CTOR_GBUTTON_1, _0010_name, "this", _0029_text, $(_0120_x), $(_0121_y), $(_0130_width), $(_0131_height));
                break;
            case ICON_ONLY:
                s = Messages.build(CTOR_GBUTTON_2, _0010_name, "this", _0037_icon_file, _0036_nbr_tiles, $(_0120_x), $(_0121_y), $(_0130_width), $(_0131_height));
                break;
            case TEXT_AND_ICON:
                s = Messages.build(CTOR_GBUTTON_3, _0010_name, "this", _0029_text, _0037_icon_file, _0036_nbr_tiles, $(_0120_x), $(_0121_y), $(_0130_width), $(_0131_height));
                break;
        }
        s += Messages.build(BTN_TEXT_ALIGN, _0010_name, _0033_xtAlignment, _0034_ytAlignment);
        if (icon != null) s += Messages.build(BTN_ICON_ALIGN, _0010_name, _0038_icon_alignment);
        s += Messages.build(ADD_HANDLER, _0010_name, "this", _0701_eventHandler);
        return s;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        NameGen.instance().add(_0010_name);
        IdGen.instance().add(id[0]);
        btn_style_editor = new EditorJComboBox(BUTTON_STYLE);
        icon_file_editor = new EditorJFileChooser();
        xtAlignment_editor = new EditorJComboBox(H_ALIGN_3);
        ytAlignment_editor = new EditorJComboBox(V_ALIGN);
        icon_alignment_editor = new EditorJComboBox(H_ALIGN_3);
        if (_0037_icon_file.length() > 0) icon = getImageFromDataFolder(_0037_icon_file);
        mitre = 6.0f;
        face = new RoundRectangle2D.Float(0, 0, _0130_width, _0131_height, mitre, mitre);
    }
}
