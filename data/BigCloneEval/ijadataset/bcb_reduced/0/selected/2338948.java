package net.sourceforge.hlm.impl.visual;

import java.util.*;
import net.sourceforge.hlm.generic.*;
import net.sourceforge.hlm.generic.annotations.*;
import net.sourceforge.hlm.util.storage.*;
import net.sourceforge.hlm.visual.*;

public abstract class LanguageSpecificImpl<T> implements LanguageSpecific<T> {

    public LanguageSpecificImpl(StoredObject parent, int index) {
        this.parent = parent;
        this.index = index;
    }

    public LanguageSpecificIteratorImpl iterator() {
        return new LanguageSpecificIteratorImpl();
    }

    protected StoredObject getContentObject(boolean create) {
        if (this.contentObject == null) {
            if (create) {
                this.contentObject = this.parent.createChildPlaceholder(this.index, Id.LANGUAGE_SPECIFIC, this.getTypeID());
            } else {
                this.contentObject = this.parent.getChild(this.index, Id.LANGUAGE_SPECIFIC, this.getTypeID());
            }
        }
        return this.contentObject;
    }

    protected static int findLanguage(StoredObject contentObject, int count, Language language, boolean single, boolean add) {
        StoredObject languageObject = null;
        if (language != null) {
            languageObject = ((LanguageImpl) language).storedObject;
        }
        return findLanguage(contentObject, 0, count, languageObject, single, add);
    }

    protected static int findLanguage(StoredObject contentObject, int start, int end, StoredObject language, boolean single, boolean add) {
        if (language == null) {
            if (single && start < end && contentObject.getReference(start) == null) {
                return start;
            } else if (add) {
                for (; start < end; start++) {
                    if (contentObject.getReference(start) != null) {
                        break;
                    }
                }
            }
        } else {
            String name = LanguageImpl.getLanguageName(language);
            while (start < end) {
                int mid = (start + end) / 2;
                int result = LanguageImpl.compareLanguageName(contentObject.getReference(mid, Id.LANGUAGE, SubId.NONE), name);
                if (single && result == 0) {
                    return mid;
                } else if (result >= (add ? 1 : 0)) {
                    end = mid;
                } else {
                    start = mid + 1;
                }
            }
        }
        if (add) {
            contentObject.insertReference(start, language);
            return ~start;
        } else if (single) {
            return end;
        } else {
            return start;
        }
    }

    protected short getTypeID() {
        return 0;
    }

    protected abstract T get(StoredObject contentObject, int index);

    protected abstract void remove(StoredObject contentObject, int index);

    void removeLanguage(StoredObject contentObject, int index) {
        Transaction transaction = contentObject.getCollection().startTransaction();
        try {
            contentObject.removeReference(index);
            this.remove(contentObject, index);
            transaction.commit();
        } finally {
            transaction.close();
        }
    }

    public StoredObject parent;

    public int index;

    private StoredObject contentObject;

    class LanguageSpecificIteratorImpl implements MapIterator<Language, T> {

        LanguageSpecificIteratorImpl() {
            this.contentObject = LanguageSpecificImpl.this.getContentObject(false);
            if (this.contentObject != null) {
                this.endIndex = this.contentObject.getReferenceCount();
            }
        }

        public boolean hasNext() {
            return (this.index < this.endIndex);
        }

        public LanguageImpl next() {
            StoredObject storedLanguage = this.contentObject.getReference(this.index++, Id.LANGUAGE, SubId.NONE);
            return (storedLanguage == null ? null : new LanguageImpl(storedLanguage));
        }

        public void remove() {
            LanguageSpecificImpl.this.removeLanguage(this.contentObject, --this.index);
        }

        public T getTarget() {
            return LanguageSpecificImpl.this.get(this.contentObject, this.index - 1);
        }

        private StoredObject contentObject;

        private int index;

        private int endIndex;
    }

    class SingleLanguageIteratorImpl implements Iterator<T> {

        SingleLanguageIteratorImpl(Language language) {
            this.contentObject = LanguageSpecificImpl.this.getContentObject(false);
            if (this.contentObject == null) {
                return;
            }
            this.endIndex = this.contentObject.getReferenceCount();
            if (this.endIndex == 0) {
                return;
            }
            if (this.endIndex == 1 && this.contentObject.getReference(0) == null) {
                this.hasNextCached = true;
                return;
            }
            this.language = (language == null ? null : ((LanguageImpl) language).storedObject);
            for (; ; ) {
                this.index = findLanguage(this.contentObject, 0, this.endIndex, this.language, false, false);
                if (this.hasNext()) {
                    break;
                } else {
                    this.endIndex = this.index;
                    if (this.language == null) {
                        break;
                    } else {
                        this.language = LanguageImpl.getParentLanguage(this.language);
                    }
                }
            }
        }

        public boolean hasNext() {
            if (!this.hasNextCached && this.index < this.endIndex) {
                StoredObject nextLanguage = this.contentObject.getReference(this.index);
                if (this.language == null) {
                    this.hasNextCached = (nextLanguage == null);
                } else {
                    this.hasNextCached = this.language.equals(nextLanguage);
                }
            }
            return this.hasNextCached;
        }

        public T next() {
            this.hasNextCached = false;
            return LanguageSpecificImpl.this.get(this.contentObject, this.index++);
        }

        public void remove() {
            LanguageSpecificImpl.this.removeLanguage(this.contentObject, --this.index);
        }

        private StoredObject contentObject;

        private int index;

        private int endIndex;

        private StoredObject language;

        private boolean hasNextCached;
    }
}
