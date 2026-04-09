package org.springframework.samples.petclinic.system;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component("imageRandomizer")
public class ImageRandomizer {

	private static final List<String> IMAGES = List.of(
            "Family-Practice-A.png",
            "Family-Practice-B.png",
            "Family-Practice-C.png"
    );

    public String getRandomImage() {
        int randomIndex = ThreadLocalRandom.current().nextInt(IMAGES.size());
        return IMAGES.get(randomIndex);
    }

}
