- 入口 AbstractBeanFactory.getBean

```
	@Override
	public Object getBean(String name) throws BeansException {
		return doGetBean(name, null, null, false);
	}
```
- 避免循环依赖
Spring在创建bean的过程 中 ,不等 bean创建完成就会将 创建bean的 ObjectFactory 提早加入到缓存中,
一旦下一个bean创建时需要依赖上一个bean 则直接使用  ObjectFactory.

- 所有 的bean 的处理后续都是 针对  RootBeanDefinition	,xml中读取的 bean信息存储 在  GenericBeanDefinition
```
AbstractBeanFactory.doGetBean
	getSingleton
	getObjectForBeanInstance
		getMergedLocalBeanDefinition
		getObjectFromFactoryBean
			AbstractAutowireCapableBeanFactory.postProcessObjectFromFactoryBean
		createBean
			AbstractAutowireCapableBeanFactory.createBean
				resolveBeforeInstantiation
				doCreateBean
					createBeanInstance
						obtainFromSupplier
						instantiateUsingFactoryMethod
						autowireConstructor
						instantiateBean
							getInstantiationStrategy().instantiate(mbd, beanName, parent)
							initBeanWrapper
					applyMergedBeanDefinitionPostProcessors
					getEarlyBeanReference -> aop
					populateBean
						autowireByName
						autowireByType
						checkDependencies
						applyPropertyValues
					registerDisposableBeanIfNecessary
```					