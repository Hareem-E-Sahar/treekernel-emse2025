public class Test {    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return ((getMember().isAnnotationPresent(annotationClass)) || (readMethod != null && readMethod.getMember().isAnnotationPresent(annotationClass)) || (writeMethod != null && writeMethod.getMember().isAnnotationPresent(annotationClass)));
    }
}