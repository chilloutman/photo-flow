package ch.zhaw.photoflow.core.domain;

import java.util.function.BooleanSupplier;

/**
 * Abstract workflow process:
 * <ol>
 * 	<li>Check and validate that the transition could be applied.</li>
 * 	<li>Actually apply the transition.</li>
 * </ol>
 * @param <S> The type of the state.
 */
abstract class AbstractWorkflow<S extends State<S>> {
	
	/**
	 * @param state The current state.
	 * @param nextState The next state to possibly transition to.
	 * @param validate Additional validation.
	 * @return {@code true} when the next state is accepted AND the validation is successful.
	 */
	protected boolean canTransition (S state, S nextState, BooleanSupplier validate) {
		return state.isValidNextState(nextState) && validate.getAsBoolean();
	}
	
	/**
	 * @param canTransition The return value of canTransition including any additional validation.
	 * @param applyTransition Run only if "canTransition" is {@code true}.
	 * @throws IllegalStateException If "canTransition" is {@code false}
	 */
	protected void transition (boolean canTransition, Runnable applyTransition) {
		if (!canTransition) {
			throw new IllegalStateException("Illegal state transition. Call canTransition() first!");
		}
		applyTransition.run();
	}
	
}
