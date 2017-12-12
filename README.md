# JpaAnnotationReader

If you need anyhow the JPA entity configuration, JpaAnnotationReader reads it for you.

But this project is retired. Preferably use [JPAAnnotationProcessor](https://github.com/dankito/jpa-apt).
It is an Annotation Processor for the APT build step
so the JPA configuration gets read at build time and there's no costly annotation reading with reflection at application start up.
