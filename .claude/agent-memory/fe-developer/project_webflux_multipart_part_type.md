---
name: WebFlux multipart file uploads use Part not FilePart for raw binary data
description: When a client sends binary data without a filename, Spring WebFlux binds it as DataBufferPart, not FilePart. Use Part as the @RequestPart type for flexibility.
type: project
---

In Spring WebFlux, `@RequestPart("image") imagePart: FilePart?` fails when the client sends a binary part without a filename (e.g. `BodyInserters.fromMultipartData("image", byteArrayOf(...))`). Spring tries to bind it as `FilePart` but gets a `DataBufferPart`, causing `IllegalArgumentException: argument type mismatch`.

**Why:** `FilePart` is specifically for file uploads with a filename header. Raw binary data is a `DataBufferPart`.

**How to apply:** Use `@RequestPart("image") imagePart: Part?` (from `org.springframework.http.codec.multipart.Part`) as the parameter type. Both `FilePart` and `DataBufferPart` implement `Part`. For content type, check `imagePart.headers().contentType` and fall back to a separately submitted `imageMimeType` string part if needed.
