# TableTap Increment 2 Completion Report

## API endpoints

| Method and path | Authentication | Request | Success response | Main failure statuses |
|---|---|---|---|---|
| `GET /api/users/me` | JWT required | None | `userId`, `fullName`, `email`, `phone`, `role`, `createdAt` | 401 unauthenticated; 404 missing account |
| `PUT /api/users/me` | JWT required | `{ fullName, email, phone }` | Updated public profile | 400 validation; 401 unauthenticated; 409 duplicate email |
| `POST /api/users/me/change-password` | JWT required | `{ currentPassword, newPassword, confirmPassword }` | 204; revokes the presented JWT | 400 incorrect current password, mismatch, same password, or validation; 401 unauthenticated |
| `POST /api/auth/forgot-password` | Public | `{ email }` | 200 `{ "message": "If an account exists, a reset link has been sent" }` | 400 malformed email; cooldown requests receive the same 200 body |
| `GET /api/auth/reset-password/validate?token=…` | Public | Reset token query parameter | 200 empty body for an active token | 400 missing, invalid, expired, or used token |
| `POST /api/auth/reset-password` | Public | `{ token, newPassword, confirmPassword }` | 204; consumes token and expires other reset tokens | 400 invalid/expired/used token, mismatch, or validation |

All profile and password operations derive the account UUID from the JWT subject. Profile request bodies cannot set account IDs, roles, password hashes, or creation timestamps. Error responses use `{ message, errors, fieldErrors }`.

## Frontend pages and routes

| Route | Access | Function |
|---|---|---|
| `/profile` | Authenticated, all roles | Loads and edits profile details; changes password with current/new/confirmation fields |
| `/forgot-password` | Public | Submits an email and shows the generic success response |
| `/reset-password?token=…` | Public | Validates the link and submits a new password |

The navbar includes Profile for DINER, STAFF, and OWNER. Profile changes update AuthContext immediately. A successful password change clears local auth and returns to login with a sign-in message.

## Database

Hibernate creates `password_reset_tokens` with UUID `id`, a required foreign key `user_id`, unique SHA-256 hex `token_hash`, `expires_at`, nullable `used_at`, and `created_at`. Raw reset tokens are not stored. Expiration is configurable and defaults to 15 minutes.

## Security measures

- Passwords use the existing BCrypt `PasswordEncoder` for registration, reset, and password change.
- Reset tokens contain 32 random bytes encoded as URL-safe Base64; only their SHA-256 hashes are persisted.
- Reset tokens expire, are single use, and are locked when redeemed. New requests invalidate earlier unused tokens.
- Forgot-password always returns the same message for existing and unknown addresses and applies an in-memory one-minute cooldown by normalized email and client IP.
- Profile routes use the JWT subject as the only owner identity. Email is lowercased and uniqueness checked before updating.
- Password changes revoke the active JWT through the existing token blocklist.
- Reset links are emailed through configured SMTP. With the `dev` profile and no SMTP sender, the link is logged for local demonstration; the raw link is not logged outside that profile.
- SMTP credentials are external configuration values and are not included in source defaults.

For Gmail, configure `SMTP_HOST=smtp.gmail.com`, `SMTP_PORT=587`, `SMTP_USERNAME` as the sending Gmail address, and `SMTP_PASSWORD` as a Google App Password. Enable 2-Step Verification before creating an App Password. Restart the backend after setting these values. The forgot-password response remains generic; delivery errors are reported in the backend log without logging the reset link or credentials.

## Automated test results

| Test case | Result |
|---|---|
| Existing registration, duplicate-email, login, and dashboard role regressions | Passed |
| Profile reads only the authenticated account, excludes password hash, persists updates, and keeps `/api/auth/me` consistent | Passed |
| Invalid profile values and duplicate email are rejected; crafted body cannot change another user | Passed |
| Forgot-password response is identical for existing and unknown emails; old unused token is invalidated and token hash is stored | Passed |
| Valid reset changes password and login; old password and token reuse fail | Passed |
| Expired/invalid token, weak password, and mismatched reset confirmation fail | Passed |
| Password change checks current password/same password/mismatch, changes login credentials, and revokes old JWT | Passed |
| Backend suite | Passed: `./mvnw clean test` (11 tests, 0 failures, 0 errors) |
| Frontend production build | Passed: `npm run build` |

## Browser manual test checklist

### Profile

- [ ] Sign in as a DINER, STAFF, or OWNER and open Profile from the navbar.
- [ ] Confirm the form loads the signed-in account's full name, email, and phone.
- [ ] Edit full name and phone, save, and confirm the navbar name updates immediately.
- [ ] Try a blank name, a name over 100 characters, malformed email, and invalid phone; confirm inline errors appear.
- [ ] Change email to an address already owned by another account; confirm the conflict message appears and the profile is unchanged.
- [ ] Cancel a profile edit and confirm the saved values are restored.

### Forgot and reset password

- [ ] On Login, select “Forgot password?” and submit an existing email; confirm the same generic success text appears.
- [ ] Repeat with an unknown email and confirm the response is indistinguishable.
- [ ] In the `dev` profile without SMTP, retrieve the development reset URL from the backend console; with SMTP configured, use the email link.
- [ ] Open the link, set a valid password, and confirm redirect to Login with a success message.
- [ ] Confirm the new password signs in and the old password fails.
- [ ] Reuse the same reset URL and try an expired/invalid token; confirm the error state is shown.
- [ ] Submit mismatched or shorter than 8-character passwords and confirm client-side validation appears.

### Change password

- [ ] From Profile, try an incorrect current password, mismatched confirmation, a weak password, and the current password as the replacement.
- [ ] Submit a valid new password; confirm redirect to Login with “Password changed, please sign in again”.
- [ ] Confirm the old password fails and the new password signs in.
- [ ] Confirm the old browser JWT no longer accesses authenticated API routes.
