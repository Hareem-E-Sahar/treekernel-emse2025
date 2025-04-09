package net.sourceforge.hlm.impl.visual;

import java.util.*;
import net.sourceforge.hlm.generic.*;
import net.sourceforge.hlm.generic.annotations.*;
import net.sourceforge.hlm.generic.exceptions.*;
import net.sourceforge.hlm.impl.*;
import net.sourceforge.hlm.util.*;
import net.sourceforge.hlm.util.storage.*;
import net.sourceforge.hlm.visual.*;

public class LanguageListImpl extends HLMObjectImpl implements LanguageList {

    public LanguageListImpl(StoredObject storedObject, String prefix) {
        super(storedObject);
        this.prefix = prefix;
    }

    public LanguageImpl add(String name) throws InvalidValueException, AlreadyFilledException {
        if (this.prefix != null && !name.startsWith(this.prefix)) {
            throw new InvalidValueException(Translator.format("language \"%s\" does not start with \"%s\"", name, this.prefix));
        }
        LanguageImpl result;
        Transaction transaction = this.storedObject.getCollection().startTransaction();
        try {
            result = this.find(name, false, true);
            transaction.commit();
        } finally {
            transaction.close();
        }
        if (result == null) {
            throw new AlreadyFilledException(Translator.format("language \"%s\" already exists", name));
        }
        return result;
    }

    public LanguageImpl get(String name) throws ItemNotFoundException {
        LanguageImpl result = this.find(name, false, false);
        if (result == null) {
            throw new ItemNotFoundException(Translator.format("language \"%s\" not found", name));
        }
        return result;
    }

    public LanguageImpl findBestMatch(String name) {
        LanguageImpl result = this.find(name, true, false);
        if (result == null) {
            result = this.find(Language.DEFAULT, true, false);
        }
        return result;
    }

    public boolean isEmpty() {
        return (this.storedObject.getChildCount() == 0);
    }

    public LanguageIteratorImpl iterator() {
        return new LanguageIteratorImpl();
    }

    private LanguageImpl find(String name, boolean bestMatch, boolean add) {
        int prefixLength = 0;
        if (this.prefix != null) {
            if (!name.startsWith(this.prefix)) {
                return null;
            }
            prefixLength = this.prefix.length();
        }
        String partialName;
        int pos = name.indexOf('-', prefixLength);
        if (pos >= 0) {
            partialName = name.substring(0, pos);
        } else {
            partialName = name;
            name = null;
        }
        int start = 0;
        int end = this.storedObject.getChildCount();
        while (start < end) {
            int mid = (start + end) / 2;
            StoredObject language = this.storedObject.getChild(mid, Id.LANGUAGE, SubId.NONE);
            int result = LanguageImpl.compareLanguageName(language, partialName);
            if (result > 0) {
                end = mid;
            } else if (result < 0) {
                start = mid + 1;
            } else {
                if (name == null) {
                    if (add) {
                        return null;
                    } else {
                        return new LanguageImpl(language);
                    }
                } else {
                    LanguageImpl partial = new LanguageImpl(language);
                    LanguageImpl full = partial.getSubLanguages().find(name, bestMatch, add);
                    if (full == null && bestMatch) {
                        return partial;
                    } else {
                        return full;
                    }
                }
            }
        }
        if (add) {
            StoredObject language = this.storedObject.insertChild(start, Id.LANGUAGE, SubId.NONE);
            language.setString(0, partialName);
            LanguageImpl partial = new LanguageImpl(language);
            if (name == null) {
                return partial;
            } else {
                return partial.getSubLanguages().find(name, bestMatch, add);
            }
        } else {
            return null;
        }
    }

    private String prefix;

    public static StoredObject getParentLanguage(StoredObject list) {
        StoredObject parent = list.getParent();
        if (parent.getTypeID() == Id.LANGUAGE) {
            return parent;
        } else {
            return null;
        }
    }

    class LanguageIteratorImpl implements Iterator<Language> {

        LanguageIteratorImpl() {
            this.endIndex = LanguageListImpl.this.storedObject.getChildCount();
        }

        public boolean hasNext() {
            return (this.index < this.endIndex);
        }

        public Language next() {
            this.last = new LanguageImpl(LanguageListImpl.this.storedObject.getChild(this.index++, Id.LANGUAGE, SubId.NONE));
            return this.last;
        }

        public void remove() {
            this.last.remove();
            this.index--;
        }

        private int index;

        private int endIndex;

        private LanguageImpl last;
    }
}
