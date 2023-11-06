package com.ikubinfo.plumbershop;

import com.ikubinfo.plumbershop.optaplanner.model.ScheduleDocument;
import com.ikubinfo.plumbershop.optaplanner.model.ShiftDocument;
import com.ikubinfo.plumbershop.optaplanner.solver.EmployeeSchedulingConstraintProvider;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.SolverManagerConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@SpringBootApplication
@EnableMongoAuditing
@EnableAspectJAutoProxy
@EnableScheduling
public class PlumberShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlumberShopApplication.class, args);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SolverManager<ScheduleDocument, String> solverManager() {
		SolverConfig solverConfig =  new SolverConfig()
				.withSolutionClass(ScheduleDocument.class)
				.withEntityClasses(ShiftDocument.class)
				.withConstraintProviderClass(EmployeeSchedulingConstraintProvider.class)
				.withTerminationConfig(new TerminationConfig().withSecondsSpentLimit(10L));
		return SolverManager.create(solverConfig, new SolverManagerConfig());
	}

}
