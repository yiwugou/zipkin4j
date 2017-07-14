package com.yiwugou.zipkin4j.example.mysql;

import java.net.InetAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Properties;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StatementInterceptorV2;
import com.yiwugou.zipkin4j.client.ZipkinClient;
import com.yiwugou.zipkin4j.client.util.InetAddressUtils;
import com.yiwugou.zipkin4j.client.util.TraceKeys;

import zipkin.Endpoint;

/**
 * spring.xml
 *
 * <pre>
 * {@code
< bean class="package.ZipkinMySQLInterceptorManagementBean" destroy-method="close">
  < constructor-argvalue="#{brave.clientTracer()}"/>
< /bean>
 * }
 * </pre>
 *
 * jdbc.properties 连接字符串添加
 *
 * <pre>
 * ?statementInterceptors=package.ZipkinMySQLInterceptor
 * </pre>
 */
public class ZipkinMySQLInterceptor implements StatementInterceptorV2 {
    private final static String SERVICE_NAME_KEY = "zipkinServiceName";

    private static volatile ZipkinClient zipkinClient;
    private static volatile String serviceName;

    public static void setZeroZipkin(final ZipkinClient zipkin) {
        zipkinClient = zipkin;
    }

    public static void setServiceName(final String service) {
        serviceName = service;
    }

    @Override
    public ResultSetInternalMethods preProcess(final String sql, final Statement interceptedStatement,
            final Connection connection) throws SQLException {
        if (zipkinClient != null) {
            final String sqlToLog;
            if (interceptedStatement instanceof PreparedStatement) {
                sqlToLog = ((PreparedStatement) interceptedStatement).getPreparedSql();
            } else {
                sqlToLog = sql;
            }

            this.beginTrace(sqlToLog, connection);
        }

        return null;
    }

    @Override
    public ResultSetInternalMethods postProcess(final String sql, final Statement interceptedStatement,
            final ResultSetInternalMethods originalResultSet, final Connection connection, final int warningCount,
            final boolean noIndexUsed, final boolean noGoodIndexUsed, final SQLException statementException)
            throws SQLException {
        if (zipkinClient == null) {
            return null;
        }
        Endpoint endpoint = this.createEndpoint(connection);
        zipkinClient.sendAnnotation(TraceKeys.CLIENT_RECV, endpoint);
        try {
            if (warningCount > 0) {
                zipkinClient.sendBinaryAnnotation("warning.count", warningCount + "", endpoint);
            }
            if (statementException != null) {
                zipkinClient.sendBinaryAnnotation("error.code", statementException.getErrorCode() + "", endpoint);
            }
        } finally {
            zipkinClient.finishSpan();
        }
        return null;
    }

    private void beginTrace(final String sql, final Connection connection) throws SQLException {
        Endpoint endpoint = this.createEndpoint(connection);
        zipkinClient.startSpan("query");
        zipkinClient.sendAnnotation(TraceKeys.CLIENT_SEND, endpoint);
        zipkinClient.sendBinaryAnnotation(TraceKeys.SQL_QUERY, sql, endpoint);
    }

    private Endpoint createEndpoint(Connection connection) {
        try {
            InetAddress address = InetAddress.getByName(connection.getHost());
            int ipv4 = ByteBuffer.wrap(address.getAddress()).getInt();
            URI url = URI.create(connection.getMetaData().getURL().substring(5));
            int port = url.getPort() <= 0 ? 3306 : url.getPort();
            if (serviceName == null || "".equals(serviceName.trim())) {
                Properties props = connection.getProperties();
                serviceName = props.getProperty(SERVICE_NAME_KEY);
                if (serviceName == null || "".equals(serviceName.trim())) {
                    serviceName = "mysql";
                    String databaseName = connection.getCatalog();
                    if (databaseName != null && !"".equals(databaseName)) {
                        serviceName += "-" + databaseName;
                    }
                }
            }
            return Endpoint.builder().serviceName(serviceName).ipv4(ipv4).port(port).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Endpoint.builder().serviceName("mysql").ipv4(InetAddressUtils.ipToInt("127.0.0.1")).port(3306)
                    .build();
        }
    }

    @Override
    public boolean executeTopLevelOnly() {
        return true;
    }

    @Override
    public void init(final Connection connection, final Properties properties) throws SQLException {
    }

    @Override
    public void destroy() {
    }
}
