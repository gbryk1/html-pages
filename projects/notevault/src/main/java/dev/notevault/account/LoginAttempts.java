package dev.notevault.account;

import java.time.Duration;

import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/** Licznik nieudanych logowań per konto, trzymany w Redisie, żeby widziały go wszystkie instancje. */
@Component
public class LoginAttempts {

	static final int MAX_FAILURES = 5;
	static final Duration LOCK_TIME = Duration.ofMinutes(15);

	private final StringRedisTemplate redis;

	LoginAttempts(StringRedisTemplate redis) {
		this.redis = redis;
	}

	@EventListener
	void onFailure(AuthenticationFailureBadCredentialsEvent event) {
		String key = key(event.getAuthentication().getName());
		Long failures = redis.opsForValue().increment(key);
		if (failures != null && failures == 1) {
			redis.expire(key, LOCK_TIME);
		}
	}

	@EventListener
	void onSuccess(AuthenticationSuccessEvent event) {
		redis.delete(key(event.getAuthentication().getName()));
	}

	public boolean isLocked(String username) {
		String failures = redis.opsForValue().get(key(username));
		return failures != null && Integer.parseInt(failures) >= MAX_FAILURES;
	}

	private static String key(String username) {
		return "login-failures:" + username.toLowerCase();
	}

}
