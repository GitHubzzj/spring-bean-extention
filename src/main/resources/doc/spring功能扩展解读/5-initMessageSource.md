- ##### initMessageSource

```
//如果自己定义,bean的名称必须为 messageSource ,因为spring 硬编码写死了.
public static final String MESSAGE_SOURCE_BEAN_NAME = "messageSource"

	protected void initMessageSource() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		//如果配置了 messageSource ,就将配置的 messageSource 赋值给 this.messageSource 
		if (beanFactory.containsLocalBean(MESSAGE_SOURCE_BEAN_NAME)) {
			this.messageSource = beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME, MessageSource.class);
			// Make MessageSource aware of parent MessageSource.
			// 如果配置的messageSource 是 HierarchicalMessageSource 实例,那么会检查配置的父资源是否已经注册,
			// 如果没注册会将父类或者父类的 messageSource 赋值进去
			if (this.parent != null && this.messageSource instanceof HierarchicalMessageSource) {
				HierarchicalMessageSource hms = (HierarchicalMessageSource) this.messageSource;
				if (hms.getParentMessageSource() == null) {
					// Only set parent context as parent MessageSource if no parent MessageSource
					// registered already.
					hms.setParentMessageSource(getInternalParentMessageSource());
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Using MessageSource [" + this.messageSource + "]");
			}
		}
		else {
		     //如果用户没有定义配置文件,那么使用 DelegatingMessageSource 以便于作为调用 getMessage 方法的返回
			// Use empty MessageSource to be able to accept getMessage calls.
			DelegatingMessageSource dms = new DelegatingMessageSource();
			dms.setParentMessageSource(getInternalParentMessageSource());
			this.messageSource = dms;
			beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate MessageSource with name '" + MESSAGE_SOURCE_BEAN_NAME +
						"': using default [" + this.messageSource + "]");
			}
		}
	}
```        
**如果自己定义,bean的名称必须为 messageSource ,因为spring 硬编码写死了.**