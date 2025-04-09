package org.proteusframework.platformservice.persistence.packaging;

import org.proteusframework.core.api.model.INamespace;
import org.proteusframework.core.base.Namespace;
import org.proteusframework.core.util.Assert;
import org.proteusframework.platformservice.persistence.api.IMessageBeanManager;
import org.proteusframework.platformservice.persistence.api.IPackagingVisitor;
import org.proteusframework.platformservice.persistence.api.messagebean.IMessageBeanDescriptor;
import org.proteusframework.platformservice.persistence.api.messagebean.IMessageBeanDescriptorVisitor;
import org.proteusframework.platformservice.persistence.api.messagebean.IMessageBeanIndex;
import org.proteusframework.platformservice.persistence.api.messagebean.IMessageBeanProperty;
import org.proteusframework.platformservice.persistence.derivative.DerivativeFactory;
import org.proteusframework.platformservice.persistence.derivative.IDerivativeConverter;
import org.proteusframework.platformservice.persistence.messagebean.DataType;
import org.proteusframework.platformservice.persistence.messagebean.MessageBeanBuilder;
import org.proteusframework.platformservice.persistence.messagebean.UnsupportedMessageBeanException;
import org.proteusframework.platformservice.persistence.util.BeanUtil;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

final class MessageBeanDescriptorZipSerializer extends ZipSerializer implements IMessageBeanDescriptorVisitor {

    private static final Logger logger = Logger.getLogger(MessageBeanDescriptorZipSerializer.class.getCanonicalName());

    private boolean includeData;

    MessageBeanDescriptorZipSerializer(ZipFile zipFile, boolean includeData) {
        super(zipFile);
        this.includeData = includeData;
    }

    MessageBeanDescriptorZipSerializer(ZipOutputStream zipOutputStream, boolean includeData) {
        super(zipOutputStream);
        this.includeData = includeData;
    }

    @Override
    public void visit(IMessageBeanManager messageBeanManager) throws IOException {
        Assert.parameterNotNull(messageBeanManager, "Parameter 'messageBeanManager' must not be null");
        if (getDirection() == IPackagingVisitor.DIRECTION_OUT) {
            for (IMessageBeanDescriptor messageBeanDescriptor : messageBeanManager.getMessageBeanDescriptors()) {
                try {
                    ZipEntry ze = new ZipEntry(MESSAGE_BEAN_DESCRIPTOR_ENTRY + getRecordCount());
                    ze.setComment((null == messageBeanDescriptor.getDescription()) ? Namespace.NULL_TOKEN : messageBeanDescriptor.getDescription());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(baos);
                    dos.writeInt(SerializationHeader.MessageBeanDescriptor.ordinal());
                    dos.writeInt(SerializationHeader.Namespace.ordinal());
                    dos.writeUTF(Namespace.toCanonical(messageBeanDescriptor));
                    dos.writeInt(SerializationHeader.DisplayableName.ordinal());
                    dos.writeUTF(messageBeanDescriptor.getName());
                    dos.writeUTF((null == messageBeanDescriptor.getDescription()) ? Namespace.NULL_TOKEN : messageBeanDescriptor.getDescription());
                    dos.writeInt(SerializationHeader.Properties.ordinal());
                    dos.writeInt(messageBeanDescriptor.properties().size());
                    for (IMessageBeanProperty property : messageBeanDescriptor.properties()) {
                        dos.writeInt(SerializationHeader.Namespace.ordinal());
                        dos.writeUTF(Namespace.toCanonical(property));
                        dos.writeInt(SerializationHeader.DisplayableName.ordinal());
                        dos.writeUTF(property.getName());
                        dos.writeUTF((null == property.getDescription()) ? Namespace.NULL_TOKEN : property.getDescription());
                        dos.writeInt(SerializationHeader.PropertyDefinition.ordinal());
                        dos.writeBoolean(property.isPrimaryKeyMember());
                        dos.writeBoolean(property.isNullable());
                        dos.writeInt(property.getDataType().ordinal());
                        dos.writeInt(property.getLength());
                        dos.writeInt(property.getPrecision());
                        dos.writeInt(SerializationHeader.Metadata.ordinal());
                        dos.writeInt(property.getMetadata().size());
                        for (String propertyName : property.getMetadata().stringPropertyNames()) {
                            dos.writeUTF(propertyName);
                            dos.writeUTF(property.getMetadata().getProperty(propertyName));
                        }
                    }
                    dos.writeInt(SerializationHeader.Indexes.ordinal());
                    dos.writeInt(messageBeanDescriptor.indices().size());
                    for (IMessageBeanIndex index : messageBeanDescriptor.indices()) {
                        dos.writeInt(SerializationHeader.Namespace.ordinal());
                        dos.writeUTF(Namespace.toCanonical(index));
                        dos.writeBoolean(index.isUnique());
                        dos.writeInt(SerializationHeader.IndexMembers.ordinal());
                        dos.writeInt(index.getIndex().size());
                        for (IMessageBeanProperty indexProperty : index.getIndex()) {
                            dos.writeUTF(Namespace.toCanonical(indexProperty));
                        }
                    }
                    dos.writeInt(SerializationHeader.Interfaces.ordinal());
                    dos.writeInt(messageBeanDescriptor.interfaces().size());
                    for (Class interfaceClass : messageBeanDescriptor.interfaces()) {
                        dos.writeUTF(interfaceClass.getPackage().getName());
                        dos.writeUTF(interfaceClass.getSimpleName());
                    }
                    dos.writeInt(SerializationHeader.Metadata.ordinal());
                    dos.writeInt(messageBeanDescriptor.getMetadata().size());
                    for (String propertyName : messageBeanDescriptor.getMetadata().stringPropertyNames()) {
                        dos.writeUTF(propertyName);
                        dos.writeUTF(messageBeanDescriptor.getMetadata().getProperty(propertyName));
                    }
                    try {
                        byte[] data = baos.toByteArray();
                        ze.setSize(data.length);
                        zipStream.putNextEntry(ze);
                        zipStream.write(data, 0, data.length);
                        zipStream.closeEntry();
                        logger.fine("Wrote zip entry " + ze.getName());
                        incrementRecordCount();
                    } catch (Exception e) {
                        logger.warning("Unable to serialize message bean descriptor " + messageBeanDescriptor.toString() + ": " + e.getMessage());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (includeData) {
                    writeRawData(messageBeanManager, messageBeanDescriptor);
                }
            }
        } else {
            Enumeration<? extends ZipEntry> zipFiles = this.zipFile.entries();
            while (zipFiles.hasMoreElements()) {
                ZipEntry entry = zipFiles.nextElement();
                if (entry.getName().startsWith(IMessageBeanDescriptorVisitor.MESSAGE_BEAN_DESCRIPTOR_ENTRY)) {
                    DataInputStream dis = new DataInputStream(zipFile.getInputStream(entry));
                    MessageBeanBuilder builder = null;
                    try {
                        do {
                            int headerID = dis.readInt();
                            SerializationHeader header = SerializationHeader.values()[headerID];
                            switch(header) {
                                case MessageBeanDescriptor:
                                    break;
                                case Namespace:
                                    INamespace beanNamespace = Namespace.fromCanonical(dis.readUTF());
                                    builder = new MessageBeanBuilder(beanNamespace);
                                    break;
                                case DisplayableName:
                                    dis.readUTF();
                                    String description = dis.readUTF();
                                    String beanDescription = (description.equals(Namespace.NULL_TOKEN) ? null : description);
                                    builder.setDescription(beanDescription);
                                    break;
                                case Properties:
                                    int properties = dis.readInt();
                                    for (int i = 0; i < properties; i++) {
                                        dis.readInt();
                                        String ns = dis.readUTF();
                                        INamespace propertyNamespace = Namespace.fromCanonical(ns);
                                        dis.readInt();
                                        dis.readUTF();
                                        String rawPropertyDescription = dis.readUTF();
                                        String propertyDescription = (rawPropertyDescription.equals(Namespace.NULL_TOKEN) ? null : rawPropertyDescription);
                                        dis.readInt();
                                        boolean isPrimaryKeyMember = dis.readBoolean();
                                        boolean isNullable = dis.readBoolean();
                                        int dataTypeOrdinal = dis.readInt();
                                        DataType dataType = DataType.values()[dataTypeOrdinal];
                                        int propLength = dis.readInt();
                                        int propPrecision = dis.readInt();
                                        builder.mapProperty(propertyNamespace.getId(), dataType, propLength, propPrecision, isPrimaryKeyMember, isNullable);
                                        builder.setPropertyDescription(propertyNamespace.getId(), propertyDescription);
                                        dis.readInt();
                                        int metadataEntries = dis.readInt();
                                        for (int j = 0; j < metadataEntries; j++) {
                                            String propKey = dis.readUTF();
                                            String propValue = dis.readUTF();
                                            builder.addPropertyMetadata(propertyNamespace.getId(), propKey, propValue);
                                        }
                                    }
                                    break;
                                case Indexes:
                                    int indexCount = dis.readInt();
                                    logger.warning("Index entry deserialization is not yet supported");
                                    break;
                                case Interfaces:
                                    int intfCount = dis.readInt();
                                    logger.warning("Interface entry deserialization is not yet supported");
                                    break;
                                case Metadata:
                                    int metadataCount = dis.readInt();
                                    for (int k = 0; k < metadataCount; k++) {
                                        String beanPropKey = dis.readUTF();
                                        String beanPropValue = dis.readUTF();
                                        builder.addMetadata(beanPropKey, beanPropValue);
                                    }
                                    break;
                            }
                        } while (true);
                    } catch (EOFException ignore) {
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    IMessageBeanDescriptor messageBeanDescriptor = builder.build();
                    try {
                        messageBeanManager.queueMessageBeanRegistration(messageBeanDescriptor);
                    } catch (UnsupportedMessageBeanException e) {
                        e.printStackTrace();
                    }
                }
            }
            messageBeanManager.processRegistrationQueue();
            if (includeData) {
                readRawData(messageBeanManager);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void writeRawData(IMessageBeanManager messageBeanManager, IMessageBeanDescriptor messageBeanDescriptor) {
        IDerivativeConverter<DataOutputStream> derivativeConverter = DerivativeFactory.createDerivativeConverter(DataOutputStream.class);
        ZipEntry ze = new ZipEntry(messageBeanDescriptor.getCanonicalName() + "_" + DATA);
        ze.setComment((null == messageBeanDescriptor.getDescription()) ? Namespace.NULL_TOKEN : messageBeanDescriptor.getDescription());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            Class messageBeanClass = Class.forName(messageBeanDescriptor.getCanonicalName());
            PropertyDescriptor[] propertyDescriptors = BeanUtil.propertyDescriptors(messageBeanClass);
            if (null != propertyDescriptors) {
                Map<String, Method> methodMap = new HashMap<String, Method>();
                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    methodMap.put(propertyDescriptor.getName(), propertyDescriptor.getReadMethod());
                }
                for (Object o : messageBeanManager.createIterator(messageBeanClass)) {
                    for (IMessageBeanProperty beanProperty : messageBeanDescriptor.properties()) {
                        logger.fine("Deriving value of property " + beanProperty.getId());
                        if (beanProperty.getDataType() != DataType.IdentityType && beanProperty.getDataType() != DataType.TransientType) {
                            Object value = null;
                            try {
                                Method readMethod = methodMap.get(beanProperty.getId());
                                if (null != readMethod) {
                                    value = readMethod.invoke(o);
                                } else {
                                    throw new IllegalStateException("Incorrectly mapped property '" + beanProperty.getId() + "': please fix the property name in the IMessageBeanDescriptor registration");
                                }
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                            derivativeConverter.convert(dos, beanProperty.getDataType(), value, IPackagingVisitor.DIRECTION_OUT);
                        }
                    }
                }
                try {
                    dos.flush();
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    byte[] data = baos.toByteArray();
                    ze.setSize(data.length);
                    zipStream.putNextEntry(ze);
                    zipStream.write(data, 0, data.length);
                    zipStream.closeEntry();
                    zipStream.flush();
                    logger.fine("Wrote data zip entry " + ze.getName());
                } catch (Exception e) {
                    logger.warning("Unable to serialize message bean descriptor " + messageBeanDescriptor.toString() + ": " + e.getMessage());
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void readRawData(IMessageBeanManager messageBeanManager) {
        IDerivativeConverter<DataInputStream> derivativeConverter = DerivativeFactory.createDerivativeConverter(DataInputStream.class);
        Enumeration<? extends ZipEntry> zipFiles = this.zipFile.entries();
        while (zipFiles.hasMoreElements()) {
            ZipEntry entry = zipFiles.nextElement();
            String entryName = entry.getName();
            if (entryName.endsWith("_" + DATA)) {
                int nameLength = entryName.length();
                String messageBeanDescriptor = entryName.substring(0, nameLength - 1 - DATA.length());
                int lastPeriodIndex = messageBeanDescriptor.lastIndexOf(".");
                String packageName = messageBeanDescriptor.substring(0, lastPeriodIndex);
                String className = messageBeanDescriptor.substring(lastPeriodIndex + 1, messageBeanDescriptor.length());
                IMessageBeanDescriptor descriptor = messageBeanManager.getMessageBeanDescriptor(new Namespace(packageName, className));
                try {
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(zipFile.getInputStream(entry)));
                    Class messageBeanClass = Class.forName(descriptor.getCanonicalName());
                    logger.fine("Found message bean class " + descriptor.getCanonicalName());
                    Object messageBean = messageBeanClass.newInstance();
                    logger.fine("Created a new instance");
                    PropertyDescriptor[] propertyDescriptors = BeanUtil.propertyDescriptors(messageBeanClass);
                    if (null != propertyDescriptors) {
                        Map<String, Method> methodMap = new HashMap<String, Method>();
                        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                            methodMap.put(propertyDescriptor.getName(), propertyDescriptor.getWriteMethod());
                        }
                        for (IMessageBeanProperty beanProperty : descriptor.properties()) {
                            logger.fine("Deriving value of property " + beanProperty.getId());
                            if (beanProperty.getDataType() != DataType.IdentityType && beanProperty.getDataType() != DataType.TransientType) {
                                Method setter = methodMap.get(beanProperty.getId());
                                derivativeConverter.convert(dis, beanProperty.getDataType(), messageBean, setter, beanProperty);
                            }
                        }
                    }
                    messageBeanManager.saveMessage(messageBeanClass, messageBean);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
