package com.one.ioc;

/**
 * @author One
 * @description
 * @date 2018/11/01
 */
public class BeanWrapperImpl {
    private Object wrappedInstance;
    private Object wrappedClass ;
    private Object rootObject ;
    /**
     * Return the type of the wrapped JavaBean object.
     * @return the type of the wrapped bean instance,
     * or {@code null} if no wrapped object has been set
     */
    public BeanWrapperImpl(Object object) {
        this.wrappedInstance = object;
        this.wrappedClass = object.getClass();
        this.rootObject = object;
    }

    public Object getRootObject() {
        return rootObject;
    }

    public void setRootObject(Object rootObject) {
        this.rootObject = rootObject;
    }

    public Object getWrappedClass() {
        return wrappedClass;
    }

    public void setWrappedClass(Object wrappedClass) {
        this.wrappedClass = wrappedClass;
    }

    public Object getWrappedInstance() {
        return wrappedInstance;
    }

    public void setWrappedInstance(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }
}
