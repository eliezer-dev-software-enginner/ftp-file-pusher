package my_app;

import megalodonte.State;

public record FtpFormState(
        State<String> host,
        State<String> port,
        State<String> username,
        State<String> password
) {}