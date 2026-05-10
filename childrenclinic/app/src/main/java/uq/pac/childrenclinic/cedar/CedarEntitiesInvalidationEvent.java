package uq.pac.childrenclinic.cedar;

import org.springframework.context.ApplicationEvent;

public class CedarEntitiesInvalidationEvent extends ApplicationEvent {

	public CedarEntitiesInvalidationEvent(Object source) {
		super(source);
	}

}
