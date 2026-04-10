export async function onRequest(context) {
  const url = new URL(context.request.url);
  const target = `http://165.232.136.214:8080${url.pathname}${url.search}`;
  
  const response = await fetch(target, {
    method: context.request.method,
    body: ["GET", "HEAD"].includes(context.request.method) ? undefined : context.request.body,
  });

  return new Response(response.body, {
    status: response.status,
    headers: response.headers,
  });
}