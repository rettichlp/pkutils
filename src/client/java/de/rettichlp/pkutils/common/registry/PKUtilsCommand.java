package de.rettichlp.pkutils.common.registry;

import org.atteo.classindex.IndexAnnotated;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@IndexAnnotated
public @interface PKUtilsCommand {

    String label();

    String[] aliases() default {};
}
