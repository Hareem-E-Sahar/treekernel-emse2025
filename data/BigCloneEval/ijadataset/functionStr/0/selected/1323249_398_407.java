public class Test {    String getAnimationModeName() {
        switch(animationReplayMode) {
            case ANIMATION_LOOP:
                return "LOOP";
            case ANIMATION_PALINDROME:
                return "PALINDROME";
            default:
                return "ONCE";
        }
    }
}