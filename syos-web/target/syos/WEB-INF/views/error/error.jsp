<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error - SYOS</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 min-h-screen flex items-center justify-center">
    <div class="max-w-lg w-full bg-white rounded-lg shadow-md p-8">
        <div class="text-center">
            <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-100 mb-4">
                <svg class="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
            </div>
            <h2 class="text-xl font-semibold text-gray-700">An Error Occurred</h2>

            <c:if test="${not empty errorMessage}">
                <p class="text-gray-500 mt-2">${errorMessage}</p>
            </c:if>

            <c:if test="${empty errorMessage}">
                <p class="text-gray-500 mt-2">Something unexpected happened. Please try again.</p>
            </c:if>

            <div class="mt-6 space-x-4">
                <a href="javascript:history.back()"
                   class="inline-block px-4 py-2 bg-gray-200 text-gray-700 font-medium rounded-lg hover:bg-gray-300 transition">
                    Go Back
                </a>
                <a href="${pageContext.request.contextPath}/"
                   class="inline-block px-4 py-2 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 transition">
                    Go Home
                </a>
            </div>
        </div>
    </div>
</body>
</html>
