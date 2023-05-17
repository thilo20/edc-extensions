package de.sovity.edc.ext.wrapper.api.offering.services;

import de.sovity.edc.ext.wrapper.api.common.model.ConstraintDto;
import de.sovity.edc.ext.wrapper.api.common.model.PolicyDto;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.edc.policy.model.Action;
import org.eclipse.edc.policy.model.AndConstraint;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.Constraint;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.OrConstraint;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.PolicyType;

public class PolicyMappingService {

    public Policy policyDtoToPolicy(PolicyDto dto) {
        return Policy.Builder.newInstance()
                .type(PolicyType.valueOf(dto.getType().getType().toUpperCase()))
                .permission(constraintsToPermission(dto))
                .build();
    }

    private AtomicConstraint constraintDtoToAtomicConstraint(ConstraintDto dto) {
        return AtomicConstraint.Builder.newInstance()
                .leftExpression(new LiteralExpression(dto.getLeftExpression()))
                .rightExpression(new LiteralExpression(dto.getRightExpression()))
                .operator(Operator.valueOf(dto.getOperatorDto().toString()))
                .build();
    }

    private Permission constraintsToPermission(PolicyDto dto) {
        List<Constraint> constraints = new ArrayList<>();

        if (dto.getPermission().getConstraintDto() != null) {
            constraints.add(constraintDtoToAtomicConstraint(dto.getPermission().getConstraintDto()));

        }

        if (dto.getPermission().getAndConstraintDtos() != null) {
            List<Constraint> andConstraints = new ArrayList<>();
            for (ConstraintDto dtoConstraint : dto.getPermission().getAndConstraintDtos()) {
                andConstraints.add(constraintDtoToAtomicConstraint(dtoConstraint));
            }
            AndConstraint andConstraint = AndConstraint.Builder.newInstance()
                    .constraints(andConstraints)
                    .build();
            constraints.add(andConstraint);
        }

        if (dto.getPermission().getOrConstraintDtos() != null) {
            List<Constraint> orConstraints = new ArrayList<>();
            for (ConstraintDto dtoConstraint : dto.getPermission().getOrConstraintDtos()) {
                orConstraints.add(constraintDtoToAtomicConstraint(dtoConstraint));
            }
            OrConstraint orConstraint = OrConstraint.Builder.newInstance()
                    .constraints(orConstraints)
                    .build();
            constraints.add(orConstraint);
        }

        return Permission.Builder.newInstance()
                .constraints(constraints)
                .action(Action.Builder.newInstance().type(dto.getPermission().getAction()).build())
                .build();
    }
}
