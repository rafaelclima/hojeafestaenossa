# Frontend Development Plan (Vibe Coding Guide)

This document serves as the **definitive guide** for the AI model responsible for building the frontend of "Hoje A Festa É Nossa". It details the backend contract, data structures, and integration points required to build the UI screens.

---

## 1. Environment & Configuration

-   **Base URL (Local):** `http://localhost:8080`
-   **Base URL (Production):** `https://hojeafestaenossa.site`
-   **CORS:** Enabled for all origins (`*`) and methods. No special CORS handling needed on the client side.
-   **Date Format:** ISO 8601 (e.g., `2023-10-27T10:00:00Z`) is used for all timestamps (`startedAt`, `expiredAt`, `createdAt`).

---

## 2. Authentication & Security

The application has two levels of access:
1.  **Public (Guest):** Can view the slideshow and upload media. No authentication required.
2.  **Admin (Organizer):** Can moderate photos, update event settings, and view stats.
    -   **Mechanism:** Custom Header
    -   **Header Name:** `X-Admin-Token`
    -   **Token Source:** The `adminToken` is returned **only** when creating an event or fetching it with admin privileges. The frontend **MUST** store this token (e.g., localStorage) to perform admin actions.

---

## 3. API Reference

### A. Events Management

#### 1. Create Event
-   **Endpoint:** `POST /events`
-   **Purpose:** Create a new event and generate access/admin tokens.
-   **Request Body:**
    ```json
    {
      "name": "Rafael's Birthday",
      "startedAt": "2026-03-10T18:00:00Z",
      "expiredAt": "2026-03-11T02:00:00Z",
      "isPublic": true
    }
    ```
-   **Response (201 Created):**
    ```json
    {
      "id": "uuid-string",
      "name": "Rafael's Birthday",
      "startedAt": "2026-03-10T18:00:00Z",
      "expiredAt": "2026-03-11T02:00:00Z",
      "createdAt": "2026-03-08T10:00:00Z",
      "accessToken": "unique-slug-token",
      "publicAlbum": true,
      "eventUrl": "https://hojeafestaenossa.site/events?eventId=unique-slug-token",
      "adminToken": "secret-admin-token-ONLY-SHOWN-HERE"
    }
    ```
    > **Important:** Save `adminToken` immediately. It allows control over the event.

#### 2. Get Event Details (Public)
-   **Endpoint:** `GET /events/{token}`
-   **Purpose:** Load event info for the landing page or slideshow.
-   **Response (200 OK):** Same as Create Event, but **without** `adminToken` (unless the user is the creator context, but the API hides it by default in public getters usually, strictly speaking check implementation if sensitive).

#### 3. Update Event (Admin)
-   **Endpoint:** `PUT /events/{token}`
-   **Headers:** `X-Admin-Token: <admin-token>`
-   **Request Body:** Same as Create Event.
-   **Response (200 OK):** Updated event object.

#### 4. Delete Event (Admin)
-   **Endpoint:** `DELETE /events/{token}`
-   **Headers:** `X-Admin-Token: <admin-token>`
-   **Response:** `204 No Content`

#### 5. Get Event Stats (Admin)
-   **Endpoint:** `GET /events/{token}/stats`
-   **Headers:** `X-Admin-Token: <admin-token>`
-   **Response (200 OK):**
    ```json
    {
      "totalUploads": 150,
      "photoCount": 120,
      "videoCount": 30
    }
    ```

---

### B. Uploads & Media

#### 1. Upload Media (Guest)
-   **Endpoint:** `POST /uploads/events/{eventToken}`
-   **Content-Type:** `multipart/form-data`
-   **Form Fields:**
    -   `file`: The file object (image/jpeg, image/png, video/mp4, etc.). Max 50MB.
    -   `message` (optional): Text message from the guest.
-   **Response:** `202 Accepted` (Async processing).
-   **Notes:** Show a progress bar. Handle `413 Payload Too Large` errors gracefully.

#### 2. Public Slideshow Feed
-   **Endpoint:** `GET /uploads/events/{eventToken}/slideshow`
-   **Query Params:** `?page=0&size=50`
-   **Purpose:** Returns **only approved** (`visible=true`) media for the big screen.
-   **Response (200 OK):**
    ```json
    {
      "content": [
        {
          "url": "https://objectstorage.../file.jpg",
          "mediaType": "PHOTO", // or "VIDEO"
          "message": "Happy Birthday!",
          "createdAt": "2026-03-10T19:00:00Z"
        }
      ],
      "totalPages": 5,
      "totalElements": 250,
      "last": false
    }
    ```

#### 3. Moderation Feed (Admin)
-   **Endpoint:** `GET /uploads/events/{eventToken}/moderation`
-   **Headers:** `X-Admin-Token: <admin-token>`
-   **Query Params:** `?page=0&size=50`
-   **Purpose:** Returns **ALL** media (approved, rejected, pending) for the admin dashboard.
-   **Response (200 OK):**
    ```json
    {
      "content": [
        {
          "id": "uuid-upload-id",
          "url": "https://objectstorage.../file.jpg",
          "thumbnailUrl": "https://objectstorage.../thumb_file.jpg",
          "mediaType": "PHOTO",
          "message": "Congrats!",
          "createdAt": "2026-03-10T19:05:00Z",
          "visible": false
        }
      ],
      ...
    }
    ```

#### 4. Moderate Media (Approve/Reject)
-   **Endpoint:** `PUT /uploads/events/{eventToken}/{uploadId}/visibility`
-   **Headers:** `X-Admin-Token: <admin-token>`
-   **Request Body:**
    ```json
    {
      "visible": true
    }
    ```
-   **Response:** `200 OK`

#### 5. Delete Media (Admin)
-   **Endpoint:** `DELETE /uploads/{eventToken}/{uploadId}`
-   **Headers:** `X-Admin-Token: <admin-token>`
-   **Response:** `204 No Content`

---

## 4. Frontend Screens & Logic Mapping

### A. Landing Page (`/event/{token}`)
1.  **Call** `GET /events/{token}` to get event name and status.
2.  **Check** if `currentDate > expiredAt`. If so, show "Event Ended".
3.  **Display** "Upload" button and "Go to Slideshow" link.

### B. Upload Screen
1.  **Form:** File input (accept `image/*, video/*`) + Message input.
2.  **Validation:** Check file size client-side before upload (>8MB for images, >50MB for videos).
3.  **Action:** `POST /uploads/events/{token}`.
4.  **Feedback:** Success message "Sent to moderation!".

### C. Slideshow Mode (`/event/{token}/slideshow`)
1.  **Initial Load:** `GET /uploads/events/{token}/slideshow`.
2.  **Looping:** Cycle through the `content` array.
    -   **Photos:** Show for 5-10 seconds.
    -   **Videos:** Autoplay (muted by default if browser blocks, or user interaction required), show for duration.
3.  **Polling:** Re-fetch the endpoint every 30-60 seconds to get new photos.

### D. Admin Dashboard
1.  **Login:** A simple prompt for the "Admin Token" if not found in localStorage (or valid from creation flow).
2.  **Stats:** `GET /events/{token}/stats`.
3.  **Moderation Grid:** `GET /uploads/events/{token}/moderation`.
    -   Show thumbnails.
    -   Toggle `visible` state using `PUT .../visibility`.
    -   Real-time feeling: Re-fetch periodically or optimistic UI updates.

---

## 5. Error Handling

Standard Backend Error Format:
```json
{
  "code": "400",
  "message": "Specific error message here"
}
```

-   **400 Bad Request:** Invalid input (e.g., file too big, wrong type).
-   **401 Unauthorized:** Invalid `X-Admin-Token`.
-   **404 Not Found:** Event or Upload ID invalid.
-   **413 Payload Too Large:** File exceeds server limit.

---

## 6. Design System Hints (Vibe)

-   **Theme:** Festive, dark mode compatible (perfect for slideshows).
-   **Mobile First:** The upload flow is 99% mobile users scanning QR codes.
-   **Animations:** Smooth transitions for the slideshow are critical.
