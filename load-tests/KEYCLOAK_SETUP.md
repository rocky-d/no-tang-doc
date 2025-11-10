# Keycloak Configuration for Load Testing

This guide provides step-by-step instructions for configuring Keycloak 26.x to support load testing for the no-tang-doc project.

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Create Load Test Client](#create-load-test-client)
- [Configure Client Settings](#configure-client-settings)
- [Create Test User (Optional)](#create-test-user-optional)
- [Configure GitHub Secrets](#configure-github-secrets)
- [Verify Configuration](#verify-configuration)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

- Access to Keycloak Admin Console: https://auth.ntdoc.site/admin
- Admin credentials for the `ntdoc` realm
- Keycloak version: 26.3.4 or higher

---

## Create Load Test Client

### Step 1: Access Clients Page

1. Log in to Keycloak Admin Console
2. Select the **`ntdoc`** realm from the dropdown (top-left corner)
3. Navigate to **Clients** in the left sidebar

### Step 2: Create New Client

1. Click **"Create client"** button
2. Fill in the **General Settings**:
   ```
   Client type: OpenID Connect
   Client ID: load-test-client
   ```
3. Click **"Next"**

### Step 3: Configure Capability

1. On the **Capability config** page:
   ```
   Client authentication: ON (toggle switch)
   Authorization: OFF
   Authentication flow:
     ☑ Standard flow
     ☑ Direct access grants
     ☐ Implicit flow (leave unchecked)
     ☑ Service accounts roles
   ```
2. Click **"Next"**

### Step 4: Configure Login Settings

1. On the **Login settings** page:
   ```
   Root URL: (leave empty)
   Home URL: (leave empty)
   Valid redirect URIs: *
   Valid post logout redirect URIs: (leave empty)
   Web origins: *
   ```
2. Click **"Save"**

---

## Configure Client Settings

### Step 5: Get Client Secret

1. After creating the client, you'll be on the client details page
2. Navigate to the **"Credentials"** tab
3. Copy the **Client secret** value (you'll need this for GitHub Secrets)
   
   Example format: `a1b2c3d4-e5f6-7890-abcd-ef1234567890`

4. **⚠️ Important**: Store this secret securely. You won't be able to view it again without regenerating.

### Step 6: Configure Service Account Roles

1. Navigate to the **"Service accounts roles"** tab
2. Click **"Assign role"**

#### Assign Realm Roles:
3. Click **"Filter by realm roles"** (or ensure you're in the "Assign Realm roles" section)
4. Search for and select:
   - ✅ **`USER`** - Required for API access
5. Click **"Assign"**

**Note**: The service account uses client credentials flow and only needs the `USER` realm role to authenticate with the APIs. You do **NOT** need to assign client roles like `account` roles or administrative `realm-management` roles.

### Step 7: Configure Client Scopes

1. Navigate to the **"Client scopes"** tab
2. Ensure the following default scopes are assigned:
   - `email`
   - `profile`
   - `roles`
   - `web-origins`

3. If `mcp-user` scope exists, add it to **"Assigned default client scopes"**

---

## Create Test User (Optional)

If you need to test with Resource Owner Password Credentials (password grant):

### Step 8: Create Test User

1. Navigate to **"Users"** in the left sidebar
2. Click **"Create new user"**
3. Fill in user details:
   ```
   Username: load-test-user
   Email: loadtest@ntdoc.site
   Email verified: ON
   First name: Load
   Last name: Test
   ```
4. Click **"Create"**

### Step 9: Set User Password

1. After creating the user, go to the **"Credentials"** tab
2. Click **"Set password"**
3. Enter a strong password:
   ```
   Password: [strong-password-here]
   Password confirmation: [strong-password-here]
   Temporary: OFF
   ```
4. Click **"Save"**

### Step 10: Assign User Roles

1. Go to the **"Role mapping"** tab
2. Click **"Assign role"**

#### Assign Realm Roles:
3. Click **"Filter by realm roles"** (or ensure you're in the "Assign Realm roles" section)
4. Search for and select:
   - ✅ **`USER`** - Required for basic user access
   - Optional: `offline_access` (if you need refresh tokens for long-running tests)
5. Click **"Assign"**

#### Assign Client Roles (Optional):
6. Click **"Filter by clients"** to switch to "Assign Client roles" section
7. From the **Client ID** dropdown, select **`account`**
8. Assign the following account management roles:
   - ✅ **`view-profile`** - Allows user to view their own profile
   - ✅ **`manage-account`** - Allows user to manage their account
9. Click **"Assign"**

**Note**: You do **NOT** need to assign `realm-management` roles (like `manage-users`, `manage-clients`, etc.) for load testing users. These are administrative roles and should only be assigned to Keycloak administrators.

---

## Configure GitHub Secrets

### Step 11: Add Secrets to GitHub Repository

1. Go to your GitHub repository: https://github.com/rocky-d/no-tang-doc
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Click **"New repository secret"**

#### Add the following secrets:

**Secret 1: LOAD_TEST_KEYCLOAK_CLIENT_ID**
```
Name: LOAD_TEST_KEYCLOAK_CLIENT_ID
Value: load-test-client
```

**Secret 2: LOAD_TEST_KEYCLOAK_CLIENT_SECRET**
```
Name: LOAD_TEST_KEYCLOAK_CLIENT_SECRET
Value: [paste the client secret from Step 5]
```

#### Optional (for password grant tests):

**Secret 3: LOAD_TEST_USER_USERNAME**
```
Name: LOAD_TEST_USER_USERNAME
Value: load-test-user
```

**Secret 4: LOAD_TEST_USER_PASSWORD**
```
Name: LOAD_TEST_USER_PASSWORD
Value: [user password from Step 9]
```

---

## Verify Configuration

### Step 12: Test Authentication

You can verify the configuration using `curl` or manually trigger a workflow:

#### Using curl:

```bash
curl -X POST "https://auth.ntdoc.site/realms/ntdoc/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=load-test-client" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "scope=openid profile email mcp-user"
```

**Expected Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "expires_in": 300,
  "token_type": "Bearer",
  "scope": "openid profile email mcp-user"
}
```

#### Using GitHub Actions:

1. Go to **Actions** tab in your repository
2. Select **"Load Test (Manual)"** workflow
3. Click **"Run workflow"**
4. Select:
   - Service: `agent`
   - Environment: `production`
   - Scenario: `smoke`
5. Click **"Run workflow"**
6. Check the workflow logs for successful authentication

---

## Troubleshooting

### Issue: "Unauthorized client"

**Cause**: Client authentication is not enabled or client secret is incorrect.

**Solution**:
1. Check that **"Client authentication"** is ON in client settings
2. Verify the client secret in GitHub Secrets matches Keycloak
3. Regenerate client secret if needed (Credentials tab → Regenerate Secret)

### Issue: "Invalid grant"

**Cause**: Service account roles are not configured.

**Solution**:
1. Go to client → **"Service accounts roles"** tab
2. Ensure required roles are assigned
3. Check that "Service accounts roles" is enabled in capability config

### Issue: "Invalid scope"

**Cause**: Requested scope is not available in the client.

**Solution**:
1. Go to client → **"Client scopes"** tab
2. Add missing scopes to **"Assigned default client scopes"**
3. Create custom scope if `mcp-user` doesn't exist:
   - Go to **"Client scopes"** (left sidebar)
   - Create new scope: `mcp-user`
   - Add to client's assigned scopes

### Issue: "Token expired" during long-running tests

**Cause**: Access token TTL is too short.

**Solution**:
1. Go to **Realm settings** → **"Tokens"** tab
2. Adjust **"Access Token Lifespan"** (default: 5 minutes)
3. Consider setting to 15-30 minutes for load tests

### Issue: Rate limiting / Too many requests

**Cause**: Keycloak is rate-limiting authentication requests.

**Solution**:
1. Consider implementing token caching in k6 scripts
2. Reuse tokens across virtual users when possible
3. Contact Keycloak admin to adjust rate limits

---

## Security Best Practices

1. **Rotate Secrets Regularly**: Change client secrets periodically
2. **Limit Permissions**: Only grant necessary roles to the test client
3. **Monitor Usage**: Review Keycloak logs for unusual activity from test client
4. **Separate Environments**: Use different clients for dev/staging/prod
5. **Delete After Testing**: Remove test users after load testing is complete

---

## Additional Resources

- [Keycloak 26.x Documentation](https://www.keycloak.org/docs/latest/)
- [OAuth 2.0 Client Credentials Grant](https://oauth.net/2/grant-types/client-credentials/)
- [k6 OAuth Authentication](https://grafana.com/docs/k6/latest/examples/oauth-authentication/)

---

**Last Updated**: 2025-11-04  
**Keycloak Version**: 26.3.4  
**Realm**: ntdoc
